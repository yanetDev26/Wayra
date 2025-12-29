package com.app.wayra.ui.subscriptions

import androidx.lifecycle.ViewModel
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import com.app.wayra.data.repository.SubscriptionRepository

class SubscriptionViewModel : ViewModel() {

    private val subscriptionRepository = SubscriptionRepository()

    suspend fun assignPlan(student: Student?, plan: Plan?): Boolean {
        if (student == null || plan == null) return false

        val subscription = Subscription(
            studentId = student.id,
            planId = plan.id,
            startDate = System.currentTimeMillis(),
            active = true
        )

        val result = subscriptionRepository.addSubscription(subscription)
        return result.isSuccess
    }

    suspend fun changePlan(studentId: String, newPlan: Plan): Boolean {
        val subscription = Subscription(
            studentId = studentId,
            planId = newPlan.id,
            startDate = System.currentTimeMillis(),
            active = true
        )

        val result = subscriptionRepository.addSubscription(subscription)
        return result.isSuccess
    }

    suspend fun cancelSubscription(subscriptionId: String): Boolean {
        val result = subscriptionRepository.cancelSubscription(subscriptionId)
        return result.isSuccess
    }
}
