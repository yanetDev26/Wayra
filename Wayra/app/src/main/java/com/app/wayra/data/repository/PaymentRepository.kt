package com.app.wayra.data.repository

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.app.wayra.domain.DateRanges
import com.app.wayra.domain.PaymentRules
import com.app.wayra.domain.ReportCalculator
import java.util.Calendar

class PaymentRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val paymentsCollection = firestore.collection("payments")

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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val payments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Payment::class.java)?.copy(id = doc.id)
                    }
                    // Ordenar en memoria por dueDate descendente
                    val sortedPayments = payments.sortedByDescending { it.dueDate ?: 0L }
                    trySend(sortedPayments)
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
            val payments = getAllPayments()
            val pendingCount = payments.count { PaymentRules.isOutstanding(it) }
            val totalCollected = ReportCalculator.totalCollected(
                payments, DateRanges.month(System.currentTimeMillis())
            )
            Triple(0, pendingCount, totalCollected) // El primer valor (active students) vendrá de StudentRepository
        } catch (_: Exception) {
            Triple(0, 0, 0.0)
        }
    }

    // Alumnos con cuotas adeudadas este mes
    suspend fun getStudentsWithPendingPaymentsThisMonth(): Int =
        studentsWithDebtInMonth(offset = 0)

    // Alumnos con cuotas adeudadas del mes pasado
    suspend fun getStudentsWithPendingPaymentsLastMonth(): Int =
        studentsWithDebtInMonth(offset = -1)

    private suspend fun studentsWithDebtInMonth(offset: Int): Int {
        return try {
            // Forzar servidor: el número de Inicio tiene que reflejar un pago recién registrado.
            val snapshot = paymentsCollection
                .whereIn("status", listOf(PaymentStatus.PENDIENTE.name, PaymentStatus.VENCIDO.name))
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            val payments = snapshot.documents.mapNotNull { doc -> doc.toObject(Payment::class.java) }
            ReportCalculator.studentsWithDebtIn(
                payments, DateRanges.month(System.currentTimeMillis(), offset)
            )
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Todos los pagos leídos del servidor; lanza excepción si no se puede.
     * Para decisiones que escriben datos (generar cuotas): una lista vacía por
     * error o una caché vieja harían creer que nadie pagó.
     */
    suspend fun getAllPaymentsFromServer(): List<Payment> =
        paymentsCollection.get(com.google.firebase.firestore.Source.SERVER).await()
            .documents
            .mapNotNull { doc -> doc.toObject(Payment::class.java)?.copy(id = doc.id) }

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
        val now = System.currentTimeMillis()
        return ReportCalculator.monthlyIncome(getPaidPayments(), DateRanges.month(now))
    }

    private suspend fun getPaidPayments(): List<Payment> {
        return try {
            paymentsCollection
                .whereEqualTo("status", PaymentStatus.PAGADO.name)
                .get()
                .await()
                .documents
                .mapNotNull { doc -> doc.toObject(Payment::class.java)?.copy(id = doc.id) }
        } catch (_: Exception) {
            emptyList()
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

    // Obtener estadísticas de pagos pendientes (incluye vencidos)
    suspend fun getPendingPaymentsStats(): PendingPaymentsStats {
        return try {
            val snapshot = paymentsCollection
                .whereIn("status", listOf(PaymentStatus.PENDIENTE.name, PaymentStatus.VENCIDO.name))
                .get()
                .await()
            val outstanding = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
            ReportCalculator.pendingStats(outstanding, System.currentTimeMillis())
        } catch (_: Exception) {
            PendingPaymentsStats(0.0, 0.0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
        }
    }

    // Buscar pago pendiente de un estudiante para un mes específico
    suspend fun findPendingPaymentForMonth(studentId: String, dueDate: Long): Payment? {
        return try {
            val month = DateRanges.month(dueDate)
            val snapshot = paymentsCollection
                .whereEqualTo("studentId", studentId)
                .whereIn("status", listOf(PaymentStatus.PENDIENTE.name, PaymentStatus.VENCIDO.name))
                .get()
                .await()

            snapshot.documents
                .mapNotNull { doc -> doc.toObject(Payment::class.java)?.copy(id = doc.id) }
                .firstOrNull { payment -> payment.dueDate != null && payment.dueDate in month }
        } catch (_: Exception) {
            null
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
    /** Cobrado en el mes en curso. */
    val monthlyRevenue: Double,
    /** Lo que deberia entrar por mes: alumnos activos x precio del plan. */
    val expectedMonthlyIncome: Double = 0.0
)

data class PlanStatisticsReport(
    val planStats: List<PlanStats>,
    val totalActiveStudents: Int,
    val totalMonthlyRevenue: Double,
    val totalRevenue: Double,
    val totalExpectedMonthlyIncome: Double = 0.0
)
