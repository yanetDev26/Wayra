package com.app.wayra.data.repository

import com.app.wayra.data.model.Plan
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlanRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val plansCollection = firestore.collection("plans")

    // Obtener todos los planes en tiempo real
    fun getPlans(): Flow<List<Plan>> = callbackFlow {
        val subscription = plansCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val plans = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Plan::class.java)?.copy(id = doc.id)
                }
                trySend(plans)
            }
        }

        awaitClose { subscription.remove() }
    }

    // Obtener plan por ID
    suspend fun getPlanById(planId: String): Plan? {
        return try {
            val doc = plansCollection.document(planId).get().await()
            doc.toObject(Plan::class.java)?.copy(id = doc.id)
        } catch (_: Exception) {
            null
        }
    }

    // Agregar nuevo plan
    suspend fun addPlan(plan: Plan): Result<String> {
        return try {
            val docRef = plansCollection.add(plan).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar plan
    suspend fun updatePlan(planId: String, plan: Plan): Result<Unit> {
        return try {
            plansCollection.document(planId).set(plan).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar plan
    suspend fun deletePlan(planId: String): Result<Unit> {
        return try {
            plansCollection.document(planId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todos los planes (una sola vez, no Flow)
    suspend fun getPlansOnce(): List<Plan> {
        return try {
            val snapshot = plansCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Plan::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
