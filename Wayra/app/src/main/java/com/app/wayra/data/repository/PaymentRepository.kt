package com.app.wayra.data.repository

import com.app.wayra.data.model.Payment
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
        } catch (e: Exception) {
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

            // Total recaudado este mes
            val paidSnapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PAGADO.name)
                .whereGreaterThanOrEqualTo("paymentDate", startOfMonth)
                .get()
                .await()

            val totalCollected = paidSnapshot.documents.sumOf { doc ->
                doc.toObject(Payment::class.java)?.amount ?: 0.0
            }

            Triple(0, pendingCount, totalCollected) // El primer valor (active students) vendrá de StudentRepository
        } catch (e: Exception) {
            Triple(0, 0, 0.0)
        }
    }
}
