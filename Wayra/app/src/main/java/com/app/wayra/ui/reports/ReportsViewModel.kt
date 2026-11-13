package com.app.wayra.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.IncomeProjectionReport
import com.app.wayra.data.repository.MonthlyIncomeStats
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.PendingPaymentsStats
import com.app.wayra.data.repository.PlanStatisticsReport
import com.app.wayra.data.repository.StatsRepository
import com.app.wayra.data.repository.StudentRepository
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val paymentRepository = PaymentRepository()
    private val statsRepository = StatsRepository()
    private val studentRepository = StudentRepository()

    private val _monthlyIncomeStats = MutableLiveData<MonthlyIncomeStats>()
    val monthlyIncomeStats: LiveData<MonthlyIncomeStats> = _monthlyIncomeStats

    private val _pendingPaymentsStats = MutableLiveData<PendingPaymentsStats>()
    val pendingPaymentsStats: LiveData<PendingPaymentsStats> = _pendingPaymentsStats

    private val _planStatistics = MutableLiveData<PlanStatisticsReport>()
    val planStatistics: LiveData<PlanStatisticsReport> = _planStatistics

    private val _allPayments = MutableLiveData<List<Payment>>()
    val allPayments: LiveData<List<Payment>> = _allPayments

    private val _allStudents = MutableLiveData<List<Student>>()
    val allStudents: LiveData<List<Student>> = _allStudents

    private val _incomeProjection = MutableLiveData<IncomeProjectionReport>()
    val incomeProjection: LiveData<IncomeProjectionReport> = _incomeProjection

    /** id de alumno -> nombre completo, para que las filas digan de quien es cada pago. */
    private val _studentNames = MutableLiveData<Map<String, String>>(emptyMap())
    val studentNames: LiveData<Map<String, String>> = _studentNames

    private suspend fun loadStudentNames() {
        _studentNames.value = studentRepository.getAllStudents().associate { it.id to it.getFullName() }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMonthlyIncomeReport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = paymentRepository.getMonthlyIncomeDetails()
                loadStudentNames()
                _monthlyIncomeStats.value = stats
            } catch (_: Exception) {
                _monthlyIncomeStats.value = MonthlyIncomeStats(0.0, 0, emptyMap(), emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPendingPaymentsReport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = paymentRepository.getPendingPaymentsStats()
                loadStudentNames()
                _pendingPaymentsStats.value = stats
            } catch (_: Exception) {
                _pendingPaymentsStats.value = PendingPaymentsStats(0.0, 0.0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlanStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = statsRepository.getPlanStatistics()
                _planStatistics.value = stats
            } catch (_: Exception) {
                _planStatistics.value = PlanStatisticsReport(emptyList(), 0, 0.0, 0.0)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPaymentHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val payments = paymentRepository.getAllPayments()
                val students = studentRepository.getAllStudents()
                _allPayments.value = payments
                _allStudents.value = students
            } catch (_: Exception) {
                _allPayments.value = emptyList()
                _allStudents.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadIncomeProjection() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projection = statsRepository.getIncomeProjection()
                _incomeProjection.value = projection
            } catch (_: Exception) {
                _incomeProjection.value = IncomeProjectionReport(emptyList(), 0.0, 0, 0.0)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
