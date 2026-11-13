package com.app.wayra.data.repository

import com.app.wayra.domain.MonthlyDues
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/** Crea en Firestore las cuotas del mes que falten. */
class MonthlyDuesRepository {

    private val firestore = Firebase.firestore
    private val studentRepository = StudentRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val planRepository = PlanRepository()
    private val paymentRepository = PaymentRepository()

    /**
     * Genera las cuotas faltantes del mes actual y devuelve cuántas creó.
     * Corre una sola vez por mes mientras la app está abierta; si falla, se
     * reintenta la próxima vez que se abre Inicio.
     */
    suspend fun ensureCurrentMonthDues(now: Long = System.currentTimeMillis()): Int = lock.withLock {
        val key = MonthlyDues.monthKey(now)
        if (lastGeneratedMonth == key) return 0

        return try {
            val students = studentRepository.getAllStudents()
            val subscriptions = subscriptionRepository.getAllActiveSubscriptions()
            val plans = planRepository.getPlansOnce()
            // Los otros repositorios devuelven lista vacía si fallan: en ese caso
            // no se genera nada ni se marca el mes como hecho, para reintentar.
            if (students.isEmpty() || plans.isEmpty() || subscriptions.isEmpty()) return 0
            // Los pagos en cambio se leen del servidor y, si fallan, cortan acá.
            val payments = paymentRepository.getAllPaymentsFromServer()

            val dues = MonthlyDues.missingDues(students, subscriptions, plans, payments, now)
            val collection = firestore.collection("payments")
            // Firestore admite hasta 500 operaciones por lote.
            dues.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { batch.set(collection.document(it.documentId), it.payment) }
                batch.commit().await()
            }
            lastGeneratedMonth = key
            dues.size
        } catch (_: Exception) {
            0
        }
    }

    private companion object {
        val lock = Mutex()
        @Volatile var lastGeneratedMonth: Int? = null
    }
}
