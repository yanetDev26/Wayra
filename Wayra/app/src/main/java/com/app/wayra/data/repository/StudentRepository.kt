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

    // Obtener estudiante por ID
    suspend fun getStudentById(studentId: String): Student? {
        return try {
            val doc = studentsCollection.document(studentId).get().await()
            doc.toObject(Student::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
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

    // Eliminar estudiante
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            studentsCollection.document(studentId).delete().await()
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
}
