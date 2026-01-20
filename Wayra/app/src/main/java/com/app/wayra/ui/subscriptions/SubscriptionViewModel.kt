package com.app.wayra.ui.subscriptions

import androidx.lifecycle.ViewModel
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.SubscriptionRepository
import java.util.Calendar

class SubscriptionViewModel : ViewModel() {

    private val subscriptionRepository = SubscriptionRepository()
    private val paymentRepository = PaymentRepository()

    suspend fun assignPlan(student: Student?, plan: Plan?): Boolean {
        if (student == null || plan == null) return false

        val subscription = Subscription(
            studentId = student.id,
            planId = plan.id,
            startDate = System.currentTimeMillis(),
            active = true
        )

        val result = subscriptionRepository.addSubscription(subscription)

        if (result.isSuccess) {
            // Establecer la fecha de vencimiento al último día del mes actual
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val dueDate = calendar.timeInMillis

            // Verificar si ya existe un pago pendiente para este estudiante en este mes
            val existingPendingPayment = paymentRepository.findPendingPaymentForMonth(student.id, dueDate)

            if (existingPendingPayment == null) {
                // Solo crear el pago si no existe uno pendiente
                val payment = Payment(
                    studentId = student.id,
                    amount = plan.price,
                    dueDate = dueDate,
                    status = PaymentStatus.PENDIENTE,
                    notes = "Pago mensual - ${plan.activityName}"
                )

                paymentRepository.addPayment(payment)
            }
            // Si ya existe un pago pendiente, no hacer nada (evitar duplicados)
        }

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

        if (result.isSuccess) {
            // Establecer la fecha de vencimiento al último día del mes actual
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val dueDate = calendar.timeInMillis

            // Verificar si ya existe un pago pendiente para este estudiante en este mes
            val existingPendingPayment = paymentRepository.findPendingPaymentForMonth(studentId, dueDate)

            if (existingPendingPayment == null) {
                // Solo crear el pago si no existe uno pendiente
                val payment = Payment(
                    studentId = studentId,
                    amount = newPlan.price,
                    dueDate = dueDate,
                    status = PaymentStatus.PENDIENTE,
                    notes = "Pago mensual - ${newPlan.activityName}"
                )

                paymentRepository.addPayment(payment)
            }
            // Si ya existe un pago pendiente, no hacer nada (evitar duplicados)
        }

        return result.isSuccess
    }
}
