package com.app.wayra.domain

import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.domain.TestData.NOVEMBER
import com.app.wayra.domain.TestData.NOW
import com.app.wayra.domain.TestData.OCTOBER
import com.app.wayra.domain.TestData.UTC
import com.app.wayra.domain.TestData.at
import com.app.wayra.domain.TestData.paid
import com.app.wayra.domain.TestData.pending
import com.app.wayra.domain.TestData.plan
import com.app.wayra.domain.TestData.subscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportCalculatorTest {

    // =====================================================================
    // Ingresos del mes (reporte) y "Recaudado" (Inicio)
    // =====================================================================

    @Test
    fun `solo suma pagos cobrados dentro del mes`() {
        val payments = listOf(
            paid(1000.0, at(2026, 11, 1, 0)),                 // primer instante del mes
            paid(2000.0, at(2026, 11, 30, 23, 59, 59, 999)),  // último instante del mes
            paid(4000.0, at(2026, 10, 31, 23, 59, 59, 999)),  // mes anterior
            paid(8000.0, at(2026, 12, 1, 0)),                 // mes siguiente
            pending(16000.0, at(2026, 11, 30))                // no cobrado
        )
        assertEquals(3000.0, ReportCalculator.totalCollected(payments, NOVEMBER), 0.0)
    }

    @Test
    fun `un pago cobrado sin fecha de cobro no entra en ningun mes`() {
        val noDate = paid(500.0, NOW).copy(paymentDate = null)
        assertEquals(0.0, ReportCalculator.totalCollected(listOf(noDate), NOVEMBER), 0.0)
    }

    @Test
    fun `ingresos del mes agrupa por metodo y ordena del mas reciente`() {
        val payments = listOf(
            paid(1000.0, at(2026, 11, 2), method = PaymentMethod.EFECTIVO),
            paid(3000.0, at(2026, 11, 10), method = PaymentMethod.TRANSFERENCIA),
            paid(500.0, at(2026, 11, 5), method = null), // datos viejos sin método: cuenta como efectivo
            paid(9999.0, at(2026, 10, 5))
        )
        val stats = ReportCalculator.monthlyIncome(payments, NOVEMBER)

        assertEquals(4500.0, stats.totalIncome, 0.0)
        assertEquals(3, stats.totalPayments)
        assertEquals(Pair(2, 1500.0), stats.paymentsByMethod[PaymentMethod.EFECTIVO])
        assertEquals(Pair(1, 3000.0), stats.paymentsByMethod[PaymentMethod.TRANSFERENCIA])
        assertEquals(listOf(3000.0, 500.0, 1000.0), stats.payments.map { it.amount })
    }

    @Test
    fun `mes sin cobros da todo en cero`() {
        val stats = ReportCalculator.monthlyIncome(emptyList(), NOVEMBER)
        assertEquals(0.0, stats.totalIncome, 0.0)
        assertEquals(0, stats.totalPayments)
        assertTrue(stats.paymentsByMethod.isEmpty())
    }

    // =====================================================================
    // Deudas por mes (Inicio: "Pendientes este mes / mes pasado")
    // =====================================================================

    @Test
    fun `cuotas adeudadas del mes incluyen pendientes y vencidas`() {
        val payments = listOf(
            pending(1000.0, at(2026, 11, 30, 23, 59, 59, 999), studentId = "a"),
            pending(1000.0, at(2026, 11, 15), studentId = "b", status = PaymentStatus.VENCIDO),
            pending(1000.0, at(2026, 10, 31), studentId = "c"),
            paid(1000.0, NOW, studentId = "d", dueDate = at(2026, 11, 30)),
            pending(1000.0, null, studentId = "e")
        )
        assertEquals(listOf("a", "b"), ReportCalculator.outstandingIn(payments, NOVEMBER).map { it.studentId })
        assertEquals(listOf("c"), ReportCalculator.outstandingIn(payments, OCTOBER).map { it.studentId })
    }

    @Test
    fun `alumnos con deuda se cuentan una vez aunque deban dos cuotas`() {
        val payments = listOf(
            pending(1000.0, at(2026, 11, 10), studentId = "a"),
            pending(500.0, at(2026, 11, 30), studentId = "a"),
            pending(1000.0, at(2026, 11, 30), studentId = "b")
        )
        assertEquals(2, ReportCalculator.studentsWithDebtIn(payments, NOVEMBER))
    }

    // =====================================================================
    // Reporte de pagos pendientes
    // =====================================================================

    @Test
    fun `pendientes separa vencidos y proximos 7 dias`() {
        val payments = listOf(
            pending(100.0, NOW - DateRanges.DAY_MS, studentId = "a"),            // vencido
            pending(200.0, NOW + 3 * DateRanges.DAY_MS, studentId = "b"),        // próximo
            pending(400.0, NOW + 7 * DateRanges.DAY_MS, studentId = "c"),        // justo 7 días: próximo
            pending(800.0, NOW + 8 * DateRanges.DAY_MS, studentId = "a"),        // más adelante
            pending(1600.0, NOW - 40 * DateRanges.DAY_MS, studentId = "d", status = PaymentStatus.VENCIDO),
            paid(9999.0, NOW, studentId = "e")
        )
        val stats = ReportCalculator.pendingStats(payments, NOW)

        assertEquals(5, stats.pendingCount)
        assertEquals(3100.0, stats.totalPending, 0.0)
        assertEquals(2, stats.overdueCount)
        assertEquals(1700.0, stats.totalOverdue, 0.0)
        assertEquals(2, stats.upcomingSoonCount)
        assertEquals(4, stats.uniqueStudents)
    }

    @Test
    fun `pendientes incluye cuotas marcadas como vencidas`() {
        // Antes solo miraba PENDIENTE y difería de Inicio.
        val stats = ReportCalculator.pendingStats(
            listOf(pending(500.0, NOW - DateRanges.DAY_MS, status = PaymentStatus.VENCIDO)), NOW
        )
        assertEquals(1, stats.pendingCount)
        assertEquals(1, stats.overdueCount)
    }

    @Test
    fun `pendientes ordena por vencimiento y deja al final las sin fecha`() {
        val stats = ReportCalculator.pendingStats(
            listOf(
                pending(1.0, null, studentId = "sinFecha"),
                pending(1.0, NOW + DateRanges.DAY_MS, studentId = "manana"),
                pending(1.0, NOW - DateRanges.DAY_MS, studentId = "ayer")
            ),
            NOW
        )
        assertEquals(listOf("ayer", "manana", "sinFecha"), stats.allPendingPayments.map { it.studentId })
    }

    // =====================================================================
    // Suscripciones y planes
    // =====================================================================

    @Test
    fun `una suscripcion vigente por alumno activo`() {
        val subs = listOf(
            subscription("old", "a", "p1", startDate = at(2026, 1, 1)),
            subscription("new", "a", "p2", startDate = at(2026, 6, 1)),   // datos viejos: dos activas
            subscription("x", "b", "p1", startDate = at(2026, 1, 1)),     // alumno dado de baja
            subscription("y", "c", "p1", startDate = at(2026, 1, 1), active = false)
        )
        val current = ReportCalculator.currentSubscriptions(subs, activeStudentIds = setOf("a", "c"))
        assertEquals(listOf("new"), current.map { it.id })
    }

    @Test
    fun `un pago se asigna al plan por su suscripcion`() {
        val subs = listOf(subscription("s1", "a", "running", at(2026, 1, 1)))
        assertEquals("running", ReportCalculator.planIdFor(paid(1.0, NOW, studentId = "a", subscriptionId = "s1"), subs))
    }

    @Test
    fun `un pago sin suscripcion se asigna al plan vigente a su vencimiento`() {
        // Así quedaban las cuotas creadas al asignar un plan.
        val subs = listOf(
            subscription("s1", "a", "running", startDate = at(2026, 1, 1), active = false),
            subscription("s2", "a", "trail", startDate = at(2026, 9, 15))
        )
        fun planOfDue(due: Long) = ReportCalculator.planIdFor(paid(1.0, NOW, studentId = "a", dueDate = due), subs)

        assertEquals("running", planOfDue(at(2026, 8, 31, 23, 59, 59)))
        assertEquals("trail", planOfDue(at(2026, 9, 30, 23, 59, 59)))
        assertEquals("running", planOfDue(at(2025, 12, 31))) // anterior a toda suscripción: la primera
    }

    @Test
    fun `un pago de un alumno sin suscripciones no tiene plan`() {
        assertNull(ReportCalculator.planIdFor(paid(1.0, NOW, studentId = "z"), emptyList()))
    }

    @Test
    fun `estadisticas por plan`() {
        val plans = listOf(plan("running", "Running", 10000.0), plan("trail", "Trail", 15000.0), plan("yoga", "Yoga", 8000.0))
        val subs = listOf(
            subscription("r1", "a", "running", at(2026, 1, 1)),
            subscription("r2", "b", "running", at(2026, 1, 1)),
            subscription("t1", "c", "trail", at(2026, 1, 1)),
            subscription("old", "d", "trail", at(2025, 1, 1), active = false) // ex alumno de trail
        )
        val payments = listOf(
            paid(10000.0, at(2026, 11, 5), studentId = "a", subscriptionId = "r1"),
            // cuota creada al asignar plan, sin subscriptionId: igual cuenta para Running
            paid(10000.0, at(2026, 11, 6), studentId = "b", dueDate = at(2026, 11, 30, 23, 59, 59)),
            paid(10000.0, at(2026, 10, 6), studentId = "a", subscriptionId = "r1"),
            paid(15000.0, at(2025, 3, 1), studentId = "d", subscriptionId = "old"), // histórico de trail
            pending(15000.0, at(2026, 11, 30), studentId = "c", subscriptionId = "t1") // no cobrado
        )

        val report = ReportCalculator.planStatistics(plans, subs, setOf("a", "b", "c"), payments, NOVEMBER)
        val byId = report.planStats.associateBy { it.planId }

        assertEquals(listOf("running", "trail", "yoga"), report.planStats.map { it.planId })

        assertEquals(2, byId.getValue("running").activeStudents)
        assertEquals(30000.0, byId.getValue("running").totalRevenue, 0.0)
        assertEquals(20000.0, byId.getValue("running").monthlyRevenue, 0.0)
        assertEquals(20000.0, byId.getValue("running").expectedMonthlyIncome, 0.0)

        assertEquals(1, byId.getValue("trail").activeStudents)
        assertEquals(15000.0, byId.getValue("trail").totalRevenue, 0.0) // incluye la suscripción ya inactiva
        assertEquals(0.0, byId.getValue("trail").monthlyRevenue, 0.0)
        assertEquals(15000.0, byId.getValue("trail").expectedMonthlyIncome, 0.0)

        assertEquals(0, byId.getValue("yoga").activeStudents)

        assertEquals(3, report.totalActiveStudents)
        assertEquals(20000.0, report.totalMonthlyRevenue, 0.0)
        assertEquals(45000.0, report.totalRevenue, 0.0)
        assertEquals(35000.0, report.totalExpectedMonthlyIncome, 0.0)
    }

    @Test
    fun `planes con la misma cantidad de alumnos se ordenan por nombre`() {
        val plans = listOf(plan("2", "yoga", 1.0), plan("1", "Funcional", 1.0))
        val report = ReportCalculator.planStatistics(plans, emptyList(), emptySet(), emptyList(), NOVEMBER)
        assertEquals(listOf("Funcional", "yoga"), report.planStats.map { it.planName })
    }

    // =====================================================================
    // Proyección de ingresos
    // =====================================================================

    @Test
    fun `proyeccion usa suscripciones vigentes de alumnos activos`() {
        val plans = listOf(plan("running", "Running", 10000.0), plan("trail", "Trail", 15000.0))
        val subs = listOf(
            subscription("r1", "a", "running", at(2026, 1, 1)),
            subscription("t1", "b", "trail", at(2026, 1, 1)),
            subscription("x", "baja", "trail", at(2026, 1, 1)),        // alumno inactivo
            subscription("z", "c", "planBorrado", at(2026, 1, 1))      // plan que ya no existe
        )
        val payments = listOf(
            pending(10000.0, at(2026, 11, 30, 23, 59, 59), studentId = "a"),
            pending(15000.0, at(2026, 11, 20), studentId = "b", status = PaymentStatus.VENCIDO),
            pending(10000.0, at(2026, 12, 31, 23, 59, 59), studentId = "a"),
            pending(10000.0, at(2027, 5, 31), studentId = "a") // fuera de los 6 meses
        )

        val report = ReportCalculator.incomeProjection(
            plans, subs, setOf("a", "b", "c"), payments, NOW, monthsAhead = 6, zone = UTC
        )

        assertEquals(25000.0, report.monthlyExpectedIncome, 0.0)
        assertEquals(2, report.totalActiveSubscriptions)
        assertEquals(12500.0, report.averageRevenuePerStudent, 0.0)

        assertEquals(
            listOf("Noviembre", "Diciembre", "Enero", "Febrero", "Marzo", "Abril"),
            report.projections.map { it.monthName }
        )
        assertEquals(listOf(2026, 2026, 2027, 2027, 2027, 2027), report.projections.map { it.year })

        val nov = report.projections[0]
        assertEquals(2, nov.pendingPayments)          // incluye la VENCIDA
        assertEquals(25000.0, nov.pendingAmount, 0.0)
        assertEquals(1, report.projections[1].pendingPayments)
        assertEquals(0, report.projections[2].pendingPayments)
        assertTrue(report.projections.all { it.expectedIncome == 25000.0 && it.activeStudents == 2 })
    }

    @Test
    fun `proyeccion sin suscripciones no divide por cero`() {
        val report = ReportCalculator.incomeProjection(emptyList(), emptyList(), emptySet(), emptyList(), NOW, zone = UTC)
        assertEquals(0.0, report.averageRevenuePerStudent, 0.0)
        assertEquals(6, report.projections.size)
    }

    // =====================================================================
    // Historial de pagos
    // =====================================================================

    private val history = listOf(
        paid(100.0, at(2026, 11, 1), studentId = "a", method = PaymentMethod.EFECTIVO, id = "1"),
        paid(200.0, at(2026, 11, 10), studentId = "b", method = PaymentMethod.TRANSFERENCIA, id = "2"),
        pending(300.0, at(2026, 10, 31), studentId = "a", id = "3"),   // atrasada
        pending(400.0, at(2026, 11, 30), studentId = "b", id = "4")    // al día
    )

    @Test
    fun `historial sin filtros devuelve todo, del mas reciente`() {
        val result = ReportCalculator.paymentHistory(history, null, null, null, NOW)
        assertEquals(listOf("4", "2", "1", "3"), result.map { it.id })
    }

    @Test
    fun `historial filtra por alumno y metodo combinados`() {
        assertEquals(listOf("1", "3"), ReportCalculator.paymentHistory(history, "a", null, null, NOW).map { it.id })
        assertEquals(listOf("2"), ReportCalculator.paymentHistory(history, null, null, PaymentMethod.TRANSFERENCIA, NOW).map { it.id })
        assertTrue(ReportCalculator.paymentHistory(history, "a", null, PaymentMethod.TRANSFERENCIA, NOW).isEmpty())
    }

    @Test
    fun `filtro vencido encuentra cuotas pendientes atrasadas`() {
        // Antes la app nunca guardaba VENCIDO y este filtro siempre daba vacío.
        assertEquals(listOf("3"), ReportCalculator.paymentHistory(history, null, PaymentStatus.VENCIDO, null, NOW).map { it.id })
        assertEquals(listOf("4"), ReportCalculator.paymentHistory(history, null, PaymentStatus.PENDIENTE, null, NOW).map { it.id })
        assertEquals(listOf("2", "1"), ReportCalculator.paymentHistory(history, null, PaymentStatus.PAGADO, null, NOW).map { it.id })
    }
}
