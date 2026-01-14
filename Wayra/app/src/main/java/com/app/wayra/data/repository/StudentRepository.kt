package com.app.wayra.data.repository

import com.app.wayra.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val studentsCollection = firestore.collection("students")

    // Obtener todos los estudiantes en tiempo real
    fun getStudents(): Flow<List<Student>> = callbackFlow {
        val subscription = studentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val students = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(id = doc.id)
                }
                trySend(students)
            }
        }

        awaitClose { subscription.remove() }
    }

    // Agregar nuevo estudiante
    suspend fun addStudent(student: Student): Result<String> {
        return try {
            val docRef = studentsCollection.add(student).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar estudiante
    suspend fun updateStudent(studentId: String, student: Student): Result<Unit> {
        return try {
            studentsCollection.document(studentId).set(student).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar estudiante con borrado en cascada de suscripciones y pagos
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            // Obtener todas las suscripciones del estudiante
            val subscriptionsSnapshot = firestore.collection("subscriptions")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            // Usar batch para operaciones atómicas
            val batch = firestore.batch()

            // Para cada suscripción, obtener y eliminar todos los pagos relacionados
            for (subscriptionDoc in subscriptionsSnapshot.documents) {
                val subscriptionId = subscriptionDoc.id

                // Obtener todos los pagos de esta suscripción
                val paymentsSnapshot = firestore.collection("payments")
                    .whereEqualTo("subscriptionId", subscriptionId)
                    .get()
                    .await()

                // Agregar eliminación de pagos al batch
                paymentsSnapshot.documents.forEach { paymentDoc ->
                    batch.delete(paymentDoc.reference)
                }

                // Agregar eliminación de la suscripción al batch
                batch.delete(subscriptionDoc.reference)
            }

            // Agregar eliminación del estudiante al batch
            batch.delete(studentsCollection.document(studentId))

            // Ejecutar todas las eliminaciones atómicamente
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener solo estudiantes activos
    fun getActiveStudents(): Flow<List<Student>> = callbackFlow {
        val subscription = studentsCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val students = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Student::class.java)?.copy(id = doc.id)
                    }
                    trySend(students)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Migración: Agregar campo surname a todos los estudiantes existentes
    suspend fun migrateStudentsAddSurname(): Result<Int> {
        return try {
            val snapshot = studentsCollection.get().await()
            var count = 0

            snapshot.documents.forEach { doc ->
                val data = doc.data
                // Solo actualizar si no tiene el campo surname
                if (data != null && !data.containsKey("surname")) {
                    doc.reference.update("surname", "").await()
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener cantidad de estudiantes nuevos este mes
    suspend fun getNewStudentsThisMonth(): Int {
        return try {
            val calendar = java.util.Calendar.getInstance()
            // Inicio del mes actual
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startOfMonth = com.google.firebase.Timestamp(java.util.Date(calendar.timeInMillis))

            val snapshot = studentsCollection
                .whereGreaterThanOrEqualTo("registrationDate", startOfMonth)
                .get()
                .await()

            snapshot.size()
        } catch (_: Exception) {
            0
        }
    }
}
