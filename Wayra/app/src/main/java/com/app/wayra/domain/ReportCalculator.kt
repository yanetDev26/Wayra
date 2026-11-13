package com.app.wayra.domain

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Subscription
import com.app.wayra.data.repository.IncomeProjectionReport
import com.app.wayra.data.repository.MonthProjection
import com.app.wayra.data.repository.MonthlyIncomeStats
import com.app.wayra.data.repository.PendingPaymentsStats
import com.app.wayra.data.repository.PlanStatisticsReport
import com.app.wayra.data.repository.PlanStats
import java.util.TimeZone

/**
 * Cálculos de Inicio y Reportes, sin Firebase: reciben los datos ya leídos
 * y la hora actual, así que se pueden testear y dan el mismo número en todas
 * las pantallas que muestran lo mismo.
 */
object ReportCalculator {

    const val UPCOMING_DAYS = 7

    // ------------------------------------------------------------------
    // Cobros
    // ------------------------------------------------------------------

    /** Pagos cobrados dentro de [range], del más reciente al más antiguo. */
    fun paidIn(payments: List<Payment>, range: MonthRange): List<Payment> =
        payments
            .filter { it.status == PaymentStatus.PAGADO && it.paymentDate != null && it.paymentDate in range }
            .sortedByDescending { it.paymentDate }

    fun totalCollected(payments: List<Payment>, range: MonthRange): Double =
        paidIn(payments, range).sumOf { it.amount }

    fun monthlyIncome(payments: List<Payment>, range: MonthRange): MonthlyIncomeStats {
        val paid = paidIn(payments, range)
        val byMethod = paid
            .groupBy { it.paymentMethod ?: PaymentMethod.EFECTIVO }
            .mapValues { (_, list) -> Pair(list.size, list.sumOf { it.amount }) }
        return MonthlyIncomeStats(
            totalIncome = paid.sumOf { it.amount },
            totalPayments = paid.size,
            paymentsByMethod = byMethod,
            payments = paid
        )
    }

    // ------------------------------------------------------------------
    // Deudas
    // ------------------------------------------------------------------

    /** Cuotas adeudadas que vencen dentro de [range] (pantallas de Inicio). */
    fun outstandingIn(payments: List<Payment>, range: MonthRange): List<Payment> =
        payments.filter { p ->
            PaymentRules.isOutstanding(p) && p.dueDate != null && p.dueDate in range
        }

    /** Alumnos distintos con alguna cuota adeudada en [range]. */
    fun studentsWithDebtIn(payments: List<Payment>, range: MonthRange): Int =
        outstandingIn(payments, range).map { it.studentId }.distinct().size

    fun pendingStats(payments: List<Payment>, now: Long): PendingPaymentsStats {
        // Incluye VENCIDO, igual que Inicio: antes el reporte solo miraba
        // PENDIENTE y los dos lugares podían mostrar cifras distintas.
        val outstanding = payments
            .filter { PaymentRules.isOutstanding(it) }
            .sortedBy { it.dueDate ?: Long.MAX_VALUE }
        val overdue = outstanding.filter { PaymentRules.isOverdue(it, now) }
        val limit = now + UPCOMING_DAYS * DateRanges.DAY_MS
        val upcoming = outstanding.filter { p -> p.dueDate != null && p.dueDate in now..limit }

        return PendingPaymentsStats(
            totalPending = outstanding.sumOf { it.amount },
            totalOverdue = overdue.sumOf { it.amount },
            pendingCount = outstanding.size,
            overdueCount = overdue.size,
            upcomingSoonCount = upcoming.size,
            uniqueStudents = outstanding.map { it.studentId }.distinct().size,
            allPendingPayments = outstanding,
            overduePayments = overdue,
            upcomingPayments = upcoming
        )
    }

    // ------------------------------------------------------------------
    // Planes
    // ------------------------------------------------------------------

    /**
     * Una suscripción activa por alumno activo. Si por datos viejos un alumno
     * tiene dos activas, vale la más reciente; los alumnos dados de baja no
     * cuentan aunque su suscripción haya quedado activa.
     */
    fun currentSubscriptions(
        subscriptions: List<Subscription>,
        activeStudentIds: Set<String>
    ): List<Subscription> =
        subscriptions
            .filter { it.active && it.studentId in activeStudentIds }
            .groupBy { it.studentId }
            .map { (_, subs) -> subs.maxBy { it.startDate ?: 0L } }

    /**
     * Plan al que corresponde un pago. Primero por `subscriptionId`; si falta
     * (las cuotas que se crean al asignar un plan no lo guardaban), por la
     * suscripción del alumno vigente a la fecha de la cuota.
     */
    fun planIdFor(payment: Payment, subscriptions: List<Subscription>): String? {
        if (payment.subscriptionId.isNotBlank()) {
            subscriptions.firstOrNull { it.id == payment.subscriptionId }?.let { return it.planId }
        }
        val reference = payment.dueDate ?: payment.paymentDate ?: return null
        val ofStudent = subscriptions
            .filter { it.studentId == payment.studentId }
            .sortedBy { it.startDate ?: 0L }
        if (ofStudent.isEmpty()) return null
        // La cuota vence a fin de mes: vale la última suscripción iniciada
        // antes de ese vencimiento; si todas son posteriores, la primera.
        return (ofStudent.lastOrNull { (it.startDate ?: 0L) <= reference } ?: ofStudent.first()).planId
    }

    fun planStatistics(
        plans: List<Plan>,
        subscriptions: List<Subscription>,
        activeStudentIds: Set<String>,
        payments: List<Payment>,
        thisMonth: MonthRange
    ): PlanStatisticsReport {
        val current = currentSubscriptions(subscriptions, activeStudentIds)
        val paid = payments.filter { it.status == PaymentStatus.PAGADO }
        val paidByPlan = paid.groupBy { planIdFor(it, subscriptions) }

        val stats = plans.map { plan ->
            val students = current.count { it.planId == plan.id }
            val planPayments = paidByPlan[plan.id].orEmpty()
            PlanStats(
                planId = plan.id,
                planName = plan.activityName,
                price = plan.price,
                activeStudents = students,
                totalRevenue = planPayments.sumOf { it.amount },
                monthlyRevenue = planPayments
                    .filter { it.paymentDate != null && it.paymentDate in thisMonth }
                    .sumOf { it.amount },
                expectedMonthlyIncome = students * plan.price
            )
        }.sortedWith(compareByDescending<PlanStats> { it.activeStudents }.thenBy { it.planName.lowercase() })

        return PlanStatisticsReport(
            planStats = stats,
            totalActiveStudents = stats.sumOf { it.activeStudents },
            totalMonthlyRevenue = stats.sumOf { it.monthlyRevenue },
            totalRevenue = stats.sumOf { it.totalRevenue },
            totalExpectedMonthlyIncome = stats.sumOf { it.expectedMonthlyIncome }
        )
    }

    // ------------------------------------------------------------------
    // Proyección
    // ------------------------------------------------------------------

    fun incomeProjection(
        plans: List<Plan>,
        subscriptions: List<Subscription>,
        activeStudentIds: Set<String>,
        payments: List<Payment>,
        now: Long,
        monthsAhead: Int = 6,
        zone: TimeZone = TimeZone.getDefault()
    ): IncomeProjectionReport {
        val current = currentSubscriptions(subscriptions, activeStudentIds)
        val priceByPlan = plans.associate { it.id to it.price }
        // Una suscripción a un plan borrado no aporta ingreso ni cuenta como alumno que paga.
        val billable = current.filter { it.planId in priceByPlan }
        val expected = billable.sumOf { priceByPlan.getValue(it.planId) }

        val projections = (0 until monthsAhead).map { offset ->
            val range = DateRanges.month(now, offset, zone)
            val due = outstandingIn(payments, range)
            MonthProjection(
                monthName = DateRanges.monthName(range.start, zone),
                year = DateRanges.year(range.start, zone),
                expectedIncome = expected,
                pendingPayments = due.size,
                pendingAmount = due.sumOf { it.amount },
                activeStudents = billable.size
            )
        }

        return IncomeProjectionReport(
            projections = projections,
            monthlyExpectedIncome = expected,
            totalActiveSubscriptions = billable.size,
            averageRevenuePerStudent = if (billable.isEmpty()) 0.0 else expected / billable.size
        )
    }

    // ------------------------------------------------------------------
    // Historial
    // ------------------------------------------------------------------

    /**
     * Filtra el historial. El estado se compara con el estado efectivo, así
     * "Vencido" encuentra las cuotas pendientes cuya fecha ya pasó.
     */
    fun paymentHistory(
        payments: List<Payment>,
        studentId: String?,
        status: PaymentStatus?,
        method: PaymentMethod?,
        now: Long
    ): List<Payment> =
        payments
            .filter { p ->
                (studentId == null || p.studentId == studentId) &&
                    (status == null || PaymentRules.effectiveStatus(p, now) == status) &&
                    (method == null || p.paymentMethod == method)
            }
            .sortedByDescending { it.paymentDate ?: it.dueDate ?: 0L }
}
