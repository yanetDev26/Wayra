package com.app.wayra.ui.plans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Plan
import com.app.wayra.data.repository.InitialDataLoader
import com.app.wayra.data.repository.PlanRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PlansViewModel : ViewModel() {

    private val planRepository = PlanRepository()

    private val _plans = MutableLiveData<List<Plan>>().apply {
        value = emptyList()
    }
    val plans: LiveData<List<Plan>> = _plans

    init {
        loadPlans()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            planRepository.getPlans()
                .catch { e ->
                    // Log error
                }
                .collect { plans ->
                    _plans.value = plans
                }
        }
    }

    suspend fun addPlan(plan: Plan): Result<String> {
        return planRepository.addPlan(plan)
    }

    suspend fun updatePlan(planId: String, plan: Plan): Result<Unit> {
        return planRepository.updatePlan(planId, plan)
    }

    suspend fun deletePlan(planId: String): Result<Unit> {
        return planRepository.deletePlan(planId)
    }

    suspend fun loadInitialPlans() {
        InitialDataLoader.loadInitialPlans(planRepository)
    }

    fun refreshData() {
        loadPlans()
    }
}
