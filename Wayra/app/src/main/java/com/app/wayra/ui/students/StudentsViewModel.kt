package com.app.wayra.ui.students

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.StudentRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StudentsViewModel : ViewModel() {

    private val studentRepository = StudentRepository()

    private val _students = MutableLiveData<List<Student>>().apply {
        value = emptyList()
    }
    val students: LiveData<List<Student>> = _students

    private val _searchQuery = MutableLiveData<String>().apply {
        value = ""
    }
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredStudents = MutableLiveData<List<Student>>().apply {
        value = emptyList()
    }
    val filteredStudents: LiveData<List<Student>> = _filteredStudents

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            studentRepository.getStudents()
                .catch { e ->
                    // Log error
                }
                .collect { students ->
                    _students.value = students
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
        val allStudents = _students.value ?: emptyList()

        _filteredStudents.value = if (query.isBlank()) {
            allStudents
        } else {
            allStudents.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true)
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
