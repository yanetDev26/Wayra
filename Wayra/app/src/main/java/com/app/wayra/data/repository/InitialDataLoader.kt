package com.app.wayra.data.repository

import com.app.wayra.data.model.Plan

object InitialDataLoader {
    fun getInitialPlans(): List<Plan> {
        return listOf(
            Plan(
                activityName = "GYM",
                price = 35000.0,
                description = "Acceso al gimnasio con rutinas guiadas"
            ),
            Plan(
                activityName = "GYM LIBRE",
                price = 45000.0,
                description = "Acceso libre al gimnasio en cualquier horario"
            ),
            Plan(
                activityName = "RUNNING",
                price = 35000.0,
                description = "Entrenamiento de running grupal"
            ),
            Plan(
                activityName = "RUNNING + GYM",
                price = 45000.0,
                description = "Combinación de running y gimnasio"
            ),
            Plan(
                activityName = "ENTRENAMIENTO PERSONALIZADO",
                price = 55000.0,
                description = "Entrenamiento personalizado one-to-one con coach"
            )
        )
    }

    suspend fun loadInitialPlans(planRepository: PlanRepository): Result<Unit> {
        return try {
            val plans = getInitialPlans()

            for (plan in plans) {
                planRepository.addPlan(plan)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
