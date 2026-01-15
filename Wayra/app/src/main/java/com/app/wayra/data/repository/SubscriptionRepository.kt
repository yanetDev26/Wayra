package com.app.wayra.data.repository

import com.app.wayra.data.model.Subscription
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SubscriptionRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val subscriptionsCollection = firestore.collection("subscriptions")

    // Obtener todas las suscripciones
    fun getSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        val subscription = subscriptionsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val subscriptions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Subscription::class.java)?.copy(id = doc.id)
                }
                trySend(subscriptions)
            }
        }

        awaitClose { subscription.remove() }
    }

    // Obtener suscripciones por estudiante
    fun getSubscriptionsByStudent(studentId: String): Flow<List<Subscription>> = callbackFlow {
        val subscription = subscriptionsCollection
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val subscriptions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Subscription::class.java)?.copy(id = doc.id)
                    }
                    trySend(subscriptions)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Obtener suscripción activa de un estudiante
    suspend fun getActiveSubscription(studentId: String): Subscription? {
        return try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Subscription::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            null
        }
    }

    // Agregar nueva suscripción
    suspend fun addSubscription(subscription: Subscription): Result<String> {
        return try {
            // Desactivar suscripciones anteriores del mismo estudiante
            val activeSubscriptions = subscriptionsCollection
                .whereEqualTo("studentId", subscription.studentId)
                .whereEqualTo("active", true)
                .get()
                .await()

            val batch = firestore.batch()
            activeSubscriptions.documents.forEach { doc ->
                batch.update(doc.reference, "active", false)
            }

            // Agregar la nueva suscripción
            val newDocRef = subscriptionsCollection.document()
            batch.set(newDocRef, subscription)

            batch.commit().await()
            Result.success(newDocRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar suscripción
    suspend fun updateSubscription(subscriptionId: String, subscription: Subscription): Result<Unit> {
        return try {
            subscriptionsCollection.document(subscriptionId).set(subscription).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancelar suscripción
    suspend fun cancelSubscription(subscriptionId: String): Result<Unit> {
        return try {
            subscriptionsCollection.document(subscriptionId)
                .update("active", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las suscripciones activas
    suspend fun getAllActiveSubscriptions(): List<Subscription> {
        return try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("active", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obtener cantidad de estudiantes por plan
    suspend fun getStudentCountByPlan(planId: String): Int {
        return try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("planId", planId)
                .whereEqualTo("active", true)
                .get()
                .await()

            snapshot.size()
        } catch (_: Exception) {
            0
        }
    }
}
