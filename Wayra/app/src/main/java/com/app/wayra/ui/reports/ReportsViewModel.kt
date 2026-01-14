package com.app.wayra.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.repository.MonthlyIncomeStats
import com.app.wayra.data.repository.PaymentRepository
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val paymentRepository = PaymentRepository()

    private val _monthlyIncomeStats = MutableLiveData<MonthlyIncomeStats>()
    val monthlyIncomeStats: LiveData<MonthlyIncomeStats> = _monthlyIncomeStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMonthlyIncomeReport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = paymentRepository.getMonthlyIncomeDetails()
                _monthlyIncomeStats.value = stats
            } catch (_: Exception) {
                _monthlyIncomeStats.value = MonthlyIncomeStats(0.0, 0, emptyMap(), emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }
}
