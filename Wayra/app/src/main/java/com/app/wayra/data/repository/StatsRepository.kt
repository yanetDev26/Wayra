package com.app.wayra.data.repository

import com.app.wayra.domain.DateRanges
import com.app.wayra.domain.ReportCalculator

class StatsRepository {

    private val planRepository = PlanRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val paymentRepository = PaymentRepository()
    private val studentRepository = StudentRepository()

    private suspend fun activeStudentIds(): Set<String> =
        studentRepository.getAllStudents().filter { it.active }.map { it.id }.toSet()

    suspend fun getPlanStatistics(): PlanStatisticsReport {
        return try {
            ReportCalculator.planStatistics(
                plans = planRepository.getPlansOnce(),
                // Todas, no solo las activas: los cobros de un plan anterior
                // siguen siendo ingresos de ese plan.
                subscriptions = subscriptionRepository.getAllSubscriptionsOnce(),
                activeStudentIds = activeStudentIds(),
                payments = paymentRepository.getAllPayments(),
                thisMonth = DateRanges.month(System.currentTimeMillis())
            )
        } catch (_: Exception) {
            PlanStatisticsReport(emptyList(), 0, 0.0, 0.0)
        }
    }

    suspend fun getIncomeProjection(monthsAhead: Int = 6): IncomeProjectionReport {
        return try {
            ReportCalculator.incomeProjection(
                plans = planRepository.getPlansOnce(),
                subscriptions = subscriptionRepository.getAllActiveSubscriptions(),
                activeStudentIds = activeStudentIds(),
                payments = paymentRepository.getAllPayments(),
                now = System.currentTimeMillis(),
                monthsAhead = monthsAhead
            )
        } catch (_: Exception) {
            IncomeProjectionReport(emptyList(), 0.0, 0, 0.0)
        }
    }
}

// Data classes para proyección de ingresos
data class MonthProjection(
    val monthName: String,
    val year: Int,
    val expectedIncome: Double,
    val pendingPayments: Int,
    val pendingAmount: Double,
    val activeStudents: Int
)

data class IncomeProjectionReport(
    val projections: List<MonthProjection>,
    val monthlyExpectedIncome: Double,
    val totalActiveSubscriptions: Int,
    val averageRevenuePerStudent: Double
)
