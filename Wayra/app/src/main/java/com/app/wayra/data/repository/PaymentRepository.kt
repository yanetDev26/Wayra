package com.app.wayra.data.repository

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class PaymentRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val paymentsCollection = firestore.collection("payments")

    // Obtener todos los pagos en tiempo real
    fun getPayments(): Flow<List<Payment>> = callbackFlow {
        val subscription = paymentsCollection
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val payments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Payment::class.java)?.copy(id = doc.id)
                    }
                    trySend(payments)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Obtener pagos próximos a vencer
    fun getUpcomingPayments(limit: Int = 10): Flow<List<Payment>> = callbackFlow {
        val today = Calendar.getInstance().timeInMillis

        val subscription = paymentsCollection
            .whereEqualTo("status", PaymentStatus.PENDIENTE.name)
            .whereGreaterThanOrEqualTo("dueDate", today)
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val payments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Payment::class.java)?.copy(id = doc.id)
                    }
                    trySend(payments)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Obtener pagos por alumno
    fun getPaymentsByStudent(studentId: String): Flow<List<Payment>> = callbackFlow {
        val subscription = paymentsCollection
            .whereEqualTo("studentId", studentId)
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val payments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Payment::class.java)?.copy(id = doc.id)
                    }
                    trySend(payments)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Obtener pago por ID
    suspend fun getPaymentById(paymentId: String): Payment? {
        return try {
            val doc = paymentsCollection.document(paymentId).get().await()
            doc.toObject(Payment::class.java)?.copy(id = doc.id)
        } catch (_: Exception) {
            null
        }
    }

    // Registrar nuevo pago
    suspend fun addPayment(payment: Payment): Result<String> {
        return try {
            val docRef = paymentsCollection.add(payment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar pago
    suspend fun updatePayment(paymentId: String, payment: Payment): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId).set(payment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar pago
    suspend fun deletePayment(paymentId: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener estadísticas del mes actual
    suspend fun getMonthlyStats(): Triple<Int, Int, Double> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // Pagos pendientes
            val pendingSnapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PENDIENTE.name)
                .get()
                .await()
            val pendingCount = pendingSnapshot.size()

            // Total recaudado este mes - obtener todos los pagos pagados y filtrar por fecha
            val paidSnapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PAGADO.name)
                .get()
                .await()

            val totalCollected = paidSnapshot.documents
                .mapNotNull { doc -> doc.toObject(Payment::class.java) }
                .filter { payment ->
                    val paymentDate = payment.paymentDate ?: 0L
                    paymentDate >= startOfMonth
                }
                .sumOf { it.amount }

            Triple(0, pendingCount, totalCollected) // El primer valor (active students) vendrá de StudentRepository
        } catch (_: Exception) {
            Triple(0, 0, 0.0)
        }
    }

    // Obtener estudiantes con pagos pendientes este mes
    suspend fun getStudentsWithPendingPaymentsThisMonth(): Int {
        return try {
            val calendar = Calendar.getInstance()
            // Inicio del mes actual
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // Fin del mes actual
            val endCalendar = Calendar.getInstance()
            endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)
            endCalendar.set(Calendar.SECOND, 59)
            val endOfMonth = endCalendar.timeInMillis

            // Obtener todos los pagos pendientes y vencidos
            val snapshot = paymentsCollection
                .whereIn("status", listOf(PaymentStatus.PENDIENTE.name, PaymentStatus.VENCIDO.name))
                .get()
                .await()

            // Filtrar por fecha en memoria y contar estudiantes únicos
            val uniqueStudents = snapshot.documents
                .mapNotNull { doc -> doc.toObject(Payment::class.java) }
                .filter { payment ->
                    val dueDate = payment.dueDate ?: 0L
                    dueDate in startOfMonth..endOfMonth
                }
                .map { it.studentId }
                .distinct()

            uniqueStudents.size
        } catch (_: Exception) {
            0
        }
    }

    // Obtener estudiantes con pagos pendientes del mes pasado
    suspend fun getStudentsWithPendingPaymentsLastMonth(): Int {
        return try {
            val calendar = Calendar.getInstance()
            // Retroceder un mes
            calendar.add(Calendar.MONTH, -1)

            // Inicio del mes pasado
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfLastMonth = calendar.timeInMillis

            // Fin del mes pasado
            val endCalendar = Calendar.getInstance()
            endCalendar.add(Calendar.MONTH, -1)
            endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)
            endCalendar.set(Calendar.SECOND, 59)
            val endOfLastMonth = endCalendar.timeInMillis

            // Obtener todos los pagos pendientes y vencidos
            val snapshot = paymentsCollection
                .whereIn("status", listOf(PaymentStatus.PENDIENTE.name, PaymentStatus.VENCIDO.name))
                .get()
                .await()

            // Filtrar por fecha en memoria y contar estudiantes únicos
            val uniqueStudents = snapshot.documents
                .mapNotNull { doc -> doc.toObject(Payment::class.java) }
                .filter { payment ->
                    val dueDate = payment.dueDate ?: 0L
                    dueDate in startOfLastMonth..endOfLastMonth
                }
                .map { it.studentId }
                .distinct()

            uniqueStudents.size
        } catch (_: Exception) {
            0
        }
    }

    // Obtener todos los pagos del mes actual
    suspend fun getMonthlyPayments(): List<Payment> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            val snapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PAGADO.name)
                .whereGreaterThanOrEqualTo("paymentDate", startOfMonth)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obtener todos los pagos (una sola vez)
    suspend fun getAllPayments(): List<Payment> {
        return try {
            val snapshot = paymentsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obtener estadísticas detalladas de ingresos del mes
    suspend fun getMonthlyIncomeDetails(): MonthlyIncomeStats {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // Obtener todos los pagos del mes
            val snapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PAGADO.name)
                .whereGreaterThanOrEqualTo("paymentDate", startOfMonth)
                .get()
                .await()

            val payments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }

            // Calcular estadísticas
            val totalIncome = payments.sumOf { it.amount }
            val totalPayments = payments.size
            val paymentsByMethod = payments.groupBy { it.paymentMethod ?: PaymentMethod.EFECTIVO }
                .mapValues { entry ->
                    Pair(entry.value.size, entry.value.sumOf { it.amount })
                }

            MonthlyIncomeStats(
                totalIncome = totalIncome,
                totalPayments = totalPayments,
                paymentsByMethod = paymentsByMethod,
                payments = payments
            )
        } catch (_: Exception) {
            MonthlyIncomeStats(0.0, 0, emptyMap(), emptyList())
        }
    }

    // Obtener pagos pendientes
    suspend fun getPendingPayments(): List<Payment> {
        return try {
            val snapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PENDIENTE.name)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obtener pagos vencidos
    suspend fun getOverduePayments(): List<Payment> {
        return try {
            val today = Calendar.getInstance().timeInMillis

            val snapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PENDIENTE.name)
                .whereLessThan("dueDate", today)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obtener estadísticas de pagos pendientes
    suspend fun getPendingPaymentsStats(): PendingPaymentsStats {
        return try {
            val today = Calendar.getInstance().timeInMillis

            // Todos los pagos pendientes
            val allPendingSnapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PENDIENTE.name)
                .get()
                .await()

            val allPending = allPendingSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }

            // Pagos vencidos
            val overdue = allPending.filter {
                (it.dueDate ?: Long.MAX_VALUE) < today
            }

            // Pagos próximos a vencer (próximos 7 días)
            val sevenDaysFromNow = today + (7 * 24 * 60 * 60 * 1000)
            val upcomingSoon = allPending.filter {
                val dueDate = it.dueDate ?: Long.MAX_VALUE
                dueDate >= today && dueDate <= sevenDaysFromNow
            }

            // Total pendiente
            val totalPending = allPending.sumOf { it.amount }
            val totalOverdue = overdue.sumOf { it.amount }

            // Estudiantes únicos con pagos pendientes
            val uniqueStudents = allPending.map { it.studentId }.distinct().size

            PendingPaymentsStats(
                totalPending = totalPending,
                totalOverdue = totalOverdue,
                pendingCount = allPending.size,
                overdueCount = overdue.size,
                upcomingSoonCount = upcomingSoon.size,
                uniqueStudents = uniqueStudents,
                allPendingPayments = allPending,
                overduePayments = overdue,
                upcomingPayments = upcomingSoon
            )
        } catch (_: Exception) {
            PendingPaymentsStats(0.0, 0.0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
        }
    }
}

// Data class para estadísticas de ingresos mensuales
data class MonthlyIncomeStats(
    val totalIncome: Double,
    val totalPayments: Int,
    val paymentsByMethod: Map<PaymentMethod, Pair<Int, Double>>, // Método -> (cantidad, total)
    val payments: List<Payment>
)

// Data class para estadísticas de pagos pendientes
data class PendingPaymentsStats(
    val totalPending: Double,
    val totalOverdue: Double,
    val pendingCount: Int,
    val overdueCount: Int,
    val upcomingSoonCount: Int,
    val uniqueStudents: Int,
    val allPendingPayments: List<Payment>,
    val overduePayments: List<Payment>,
    val upcomingPayments: List<Payment>
)

// Data class para estadísticas por plan
data class PlanStats(
    val planId: String,
    val planName: String,
    val price: Double,
    val activeStudents: Int,
    val totalRevenue: Double,
    val monthlyRevenue: Double
)

data class PlanStatisticsReport(
    val planStats: List<PlanStats>,
    val totalActiveStudents: Int,
    val totalMonthlyRevenue: Double,
    val totalRevenue: Double
)
