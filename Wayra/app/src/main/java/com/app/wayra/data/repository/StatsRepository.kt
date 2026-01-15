package com.app.wayra.data.repository

import com.app.wayra.data.model.PaymentStatus
import java.util.Calendar

class StatsRepository {

    private val planRepository = PlanRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val paymentRepository = PaymentRepository()

    suspend fun getPlanStatistics(): PlanStatisticsReport {
        return try {
            // Obtener todos los planes
            val plans = planRepository.getPlansOnce()

            // Obtener todas las suscripciones activas
            val activeSubscriptions = subscriptionRepository.getAllActiveSubscriptions()

            // Calcular inicio del mes actual para ingresos mensuales
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // Obtener todos los pagos para calcular ingresos
            val allPayments = paymentRepository.getAllPayments()

            // Crear estadísticas por plan
            val planStats = plans.map { plan ->
                // Contar estudiantes activos en este plan
                val activeStudents = activeSubscriptions.count { it.planId == plan.id }

                // Obtener IDs de suscripciones para este plan
                val subscriptionIds = activeSubscriptions
                    .filter { it.planId == plan.id }
                    .map { it.id }

                // Calcular ingresos totales para este plan
                val totalRevenue = allPayments
                    .filter { it.subscriptionId in subscriptionIds && it.status == PaymentStatus.PAGADO }
                    .sumOf { it.amount }

                // Calcular ingresos del mes actual
                val monthlyRevenue = allPayments
                    .filter {
                        it.subscriptionId in subscriptionIds &&
                        it.status == PaymentStatus.PAGADO &&
                        (it.paymentDate ?: 0L) >= startOfMonth
                    }
                    .sumOf { it.amount }

                PlanStats(
                    planId = plan.id,
                    planName = plan.activityName,
                    price = plan.price,
                    activeStudents = activeStudents,
                    totalRevenue = totalRevenue,
                    monthlyRevenue = monthlyRevenue
                )
            }.sortedByDescending { it.activeStudents }

            // Calcular totales
            val totalActiveStudents = planStats.sumOf { it.activeStudents }
            val totalMonthlyRevenue = planStats.sumOf { it.monthlyRevenue }
            val totalRevenue = planStats.sumOf { it.totalRevenue }

            PlanStatisticsReport(
                planStats = planStats,
                totalActiveStudents = totalActiveStudents,
                totalMonthlyRevenue = totalMonthlyRevenue,
                totalRevenue = totalRevenue
            )
        } catch (_: Exception) {
            PlanStatisticsReport(
                planStats = emptyList(),
                totalActiveStudents = 0,
                totalMonthlyRevenue = 0.0,
                totalRevenue = 0.0
            )
        }
    }

    suspend fun getIncomeProjection(monthsAhead: Int = 6): IncomeProjectionReport {
        return try {
            // Obtener planes y suscripciones activas
            val plans = planRepository.getPlansOnce()
            val activeSubscriptions = subscriptionRepository.getAllActiveSubscriptions()
            val allPayments = paymentRepository.getAllPayments()

            // Calcular ingreso mensual esperado basado en suscripciones activas
            val monthlyExpectedIncome = activeSubscriptions.mapNotNull { subscription ->
                plans.find { it.id == subscription.planId }?.price
            }.sum()

            // Generar proyecciones para los próximos meses
            val calendar = Calendar.getInstance()
            val projections = mutableListOf<MonthProjection>()

            for (i in 0 until monthsAhead) {
                // Calcular inicio y fin del mes
                calendar.add(Calendar.MONTH, if (i == 0) 0 else 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfMonth = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfMonth = calendar.timeInMillis

                // Contar pagos pendientes en este mes
                val pendingPaymentsInMonth = allPayments.filter { payment ->
                    val dueDate = payment.dueDate ?: 0L
                    dueDate in startOfMonth..endOfMonth && payment.status == PaymentStatus.PENDIENTE
                }

                val pendingAmount = pendingPaymentsInMonth.sumOf { it.amount }
                val pendingCount = pendingPaymentsInMonth.size

                projections.add(
                    MonthProjection(
                        monthName = getMonthName(calendar.get(Calendar.MONTH)),
                        year = calendar.get(Calendar.YEAR),
                        expectedIncome = monthlyExpectedIncome,
                        pendingPayments = pendingCount,
                        pendingAmount = pendingAmount,
                        activeStudents = activeSubscriptions.size
                    )
                )
            }

            // Resetear calendario
            calendar.timeInMillis = System.currentTimeMillis()

            IncomeProjectionReport(
                projections = projections,
                monthlyExpectedIncome = monthlyExpectedIncome,
                totalActiveSubscriptions = activeSubscriptions.size,
                averageRevenuePerStudent = if (activeSubscriptions.isNotEmpty()) {
                    monthlyExpectedIncome / activeSubscriptions.size
                } else {
                    0.0
                }
            )
        } catch (_: Exception) {
            IncomeProjectionReport(
                projections = emptyList(),
                monthlyExpectedIncome = 0.0,
                totalActiveSubscriptions = 0,
                averageRevenuePerStudent = 0.0
            )
        }
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return monthNames[month]
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
