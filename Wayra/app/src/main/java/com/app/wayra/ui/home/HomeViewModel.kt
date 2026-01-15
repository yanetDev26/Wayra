package com.app.wayra.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Payment
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.StudentRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeStats(
    val activeStudents: Int = 0,
    val newStudentsThisMonth: Int = 0,
    val totalCollected: Double = 0.0,
    val pendingThisMonth: Int = 0,
    val pendingLastMonth: Int = 0
)

class HomeViewModel : ViewModel() {

    private val studentRepository = StudentRepository()
    private val paymentRepository = PaymentRepository()

    private val _stats = MutableLiveData<HomeStats>().apply {
        value = HomeStats()
    }
    val stats: LiveData<HomeStats> = _stats

    private val _upcomingPayments = MutableLiveData<List<Payment>>().apply {
        value = emptyList()
    }

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    init {
        loadCurrentDate()
        loadData()
    }

    private fun loadCurrentDate() {
        val dateFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-ES"))
        _currentDate.value = dateFormat.format(Date())
    }

    private fun loadData() {
        // Cargar estudiantes activos
        viewModelScope.launch {
            studentRepository.getActiveStudents()
                .catch { e ->
                    // Log error
                }
                .collect { students ->
                    val currentStats = _stats.value ?: HomeStats()
                    _stats.value = currentStats.copy(activeStudents = students.size)
                }
        }

        // Cargar pagos próximos
        viewModelScope.launch {
            paymentRepository.getUpcomingPayments(5)
                .catch { e ->
                    // Log error
                }
                .collect { payments ->
                    _upcomingPayments.value = payments
                }
        }

        // Cargar estadísticas
        viewModelScope.launch {
            val (_, _, totalCollected) = paymentRepository.getMonthlyStats()
            val pendingThisMonth = paymentRepository.getStudentsWithPendingPaymentsThisMonth()
            val pendingLastMonth = paymentRepository.getStudentsWithPendingPaymentsLastMonth()
            val newStudentsThisMonth = studentRepository.getNewStudentsThisMonth()

            val currentStats = _stats.value ?: HomeStats()
            _stats.value = currentStats.copy(
                newStudentsThisMonth = newStudentsThisMonth,
                totalCollected = totalCollected,
                pendingThisMonth = pendingThisMonth,
                pendingLastMonth = pendingLastMonth
            )
        }
    }

    fun refreshData() {
        loadData()
    }
}