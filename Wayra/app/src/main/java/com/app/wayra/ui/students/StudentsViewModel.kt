package com.app.wayra.ui.students

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.PlanRepository
import com.app.wayra.data.repository.StudentRepository
import com.app.wayra.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class StudentWithPlan(
    val student: Student,
    val planName: String? = null
)

class StudentsViewModel : ViewModel() {

    private val studentRepository = StudentRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val planRepository = PlanRepository()

    private val _students = MutableLiveData<List<Student>>().apply {
        value = emptyList()
    }
    val students: LiveData<List<Student>> = _students

    private val _studentsWithPlan = MutableLiveData<List<StudentWithPlan>>().apply {
        value = emptyList()
    }

    private val _searchQuery = MutableLiveData<String>().apply {
        value = ""
    }
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredStudents = MutableLiveData<List<StudentWithPlan>>().apply {
        value = emptyList()
    }
    val filteredStudents: LiveData<List<StudentWithPlan>> = _filteredStudents

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            combine(
                studentRepository.getStudents(),
                subscriptionRepository.getSubscriptions(),
                planRepository.getPlans()
            ) { students, subscriptions, plans ->
                students.map { student ->
                    // Encontrar la suscripción activa del estudiante
                    val activeSubscription = subscriptions.find {
                        it.studentId == student.id && it.active
                    }
                    // Encontrar el plan correspondiente
                    val plan = activeSubscription?.let { subscription ->
                        plans.find { it.id == subscription.planId }
                    }
                    StudentWithPlan(
                        student = student,
                        planName = plan?.activityName
                    )
                }
            }
                .catch { e ->
                    // Log error
                }
                .collect { studentsWithPlan ->
                    _studentsWithPlan.value = studentsWithPlan
                    _students.value = studentsWithPlan.map { it.student }
                    filterStudents()
                }
        }
    }

    fun searchStudents(query: String) {
        _searchQuery.value = query
        filterStudents()
    }

    private fun filterStudents() {
        val query = _searchQuery.value ?: ""
        val allStudentsWithPlan = _studentsWithPlan.value ?: emptyList()

        _filteredStudents.value = if (query.isBlank()) {
            allStudentsWithPlan
        } else {
            allStudentsWithPlan.filter {
                it.student.name.contains(query, ignoreCase = true) ||
                it.student.email.contains(query, ignoreCase = true) ||
                it.student.phone.contains(query, ignoreCase = true) ||
                (it.planName?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    suspend fun addStudent(student: Student): Result<String> {
        return studentRepository.addStudent(student)
    }

    suspend fun updateStudent(studentId: String, student: Student): Result<Unit> {
        return studentRepository.updateStudent(studentId, student)
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return studentRepository.deleteStudent(studentId)
    }

    fun refreshData() {
        loadStudents()
    }
}
