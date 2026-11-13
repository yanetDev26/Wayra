package com.app.wayra.domain

import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.domain.TestData.NOW
import com.app.wayra.domain.TestData.UTC
import com.app.wayra.domain.TestData.at
import com.app.wayra.domain.TestData.paid
import com.app.wayra.domain.TestData.pending
import com.app.wayra.domain.TestData.plan
import com.app.wayra.domain.TestData.student
import com.app.wayra.domain.TestData.subscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthlyDuesTest {

    private val plans = listOf(plan("running", "Running", 10000.0), plan("trail", "Trail", 15000.0))

    private fun dues(
        students: List<com.app.wayra.data.model.Student>,
        subs: List<com.app.wayra.data.model.Subscription>,
        payments: List<com.app.wayra.data.model.Payment> = emptyList(),
        now: Long = NOW
    ) = MonthlyDues.missingDues(students, subs, plans, payments, now, UTC)

    @Test
    fun `crea la cuota del mes para cada alumno activo con plan`() {
        val result = dues(
            students = listOf(student("a"), student("b")),
            subs = listOf(
                subscription("ra", "a", "running", at(2026, 1, 1)),
                subscription("tb", "b", "trail", at(2026, 3, 1))
            )
        )

        assertEquals(listOf("cuota_a_202611", "cuota_b_202611"), result.map { it.documentId })
        val a = result.first().payment
        assertEquals("a", a.studentId)
        assertEquals("ra", a.subscriptionId)
        assertEquals(10000.0, a.amount, 0.0)
        assertEquals(at(2026, 11, 30, 23, 59, 59), a.dueDate)
        assertEquals(PaymentStatus.PENDIENTE, a.status)
        assertEquals("Pago mensual - Running", a.notes)
        assertEquals(15000.0, result[1].payment.amount, 0.0)
    }

    @Test
    fun `no crea cuota si el alumno ya tiene un pago del mes, cobrado o pendiente`() {
        val students = listOf(student("pago"), student("debe"), student("adelanto"), student("nada"))
        val subs = students.map { subscription("s-${it.id}", it.id, "running", at(2026, 1, 1)) }
        val payments = listOf(
            paid(10000.0, at(2026, 11, 3), studentId = "pago", dueDate = at(2026, 11, 30, 23, 59, 59)),
            pending(10000.0, at(2026, 11, 30, 23, 59, 59, 417), studentId = "debe"),
            // pagó noviembre por adelantado en octubre
            paid(10000.0, at(2026, 10, 20), studentId = "adelanto", dueDate = at(2026, 11, 30, 23, 59, 59)),
            // un pago de otro mes no cuenta
            paid(10000.0, at(2026, 10, 3), studentId = "nada", dueDate = at(2026, 10, 31, 23, 59, 59))
        )

        assertEquals(listOf("nada"), dues(students, subs, payments).map { it.payment.studentId })
    }

    @Test
    fun `no crea cuota a alumnos inactivos, sin plan, o con plan borrado`() {
        val result = dues(
            students = listOf(student("baja", active = false), student("sinPlan"), student("borrado"), student("ok")),
            subs = listOf(
                subscription("1", "baja", "running", at(2026, 1, 1)),
                subscription("2", "sinPlan", "running", at(2026, 1, 1), active = false),
                subscription("3", "borrado", "yoga", at(2026, 1, 1)),
                subscription("4", "ok", "running", at(2026, 1, 1))
            )
        )
        assertEquals(listOf("ok"), result.map { it.payment.studentId })
    }

    @Test
    fun `con dos suscripciones activas usa la mas reciente`() {
        val result = dues(
            students = listOf(student("a")),
            subs = listOf(
                subscription("vieja", "a", "running", at(2026, 1, 1)),
                subscription("nueva", "a", "trail", at(2026, 8, 1))
            )
        )
        assertEquals(1, result.size)
        assertEquals("nueva", result.single().payment.subscriptionId)
        assertEquals(15000.0, result.single().payment.amount, 0.0)
    }

    @Test
    fun `una suscripcion que empieza el mes que viene todavia no genera cuota`() {
        val result = dues(
            students = listOf(student("a"), student("b")),
            subs = listOf(
                subscription("1", "a", "running", at(2026, 12, 1, 0)),
                subscription("2", "b", "running", at(2026, 11, 30, 23))
            )
        )
        assertEquals(listOf("b"), result.map { it.payment.studentId })
    }

    @Test
    fun `es idempotente - con las cuotas ya creadas no genera nada mas`() {
        val students = listOf(student("a"), student("b"))
        val subs = listOf(
            subscription("1", "a", "running", at(2026, 1, 1)),
            subscription("2", "b", "trail", at(2026, 1, 1))
        )
        val first = dues(students, subs)
        assertEquals(2, first.size)
        assertTrue(dues(students, subs, payments = first.map { it.payment }).isEmpty())
    }

    @Test
    fun `cada mes genera su propia cuota con su propio id`() {
        val students = listOf(student("a"))
        val subs = listOf(subscription("1", "a", "running", at(2026, 1, 1)))
        val november = dues(students, subs).single()
        val december = dues(students, subs, payments = listOf(november.payment), now = at(2026, 12, 2)).single()

        assertEquals("cuota_a_202612", december.documentId)
        assertEquals(at(2026, 12, 31, 23, 59, 59), december.payment.dueDate)
    }

    @Test
    fun `id del documento y clave del mes`() {
        assertEquals("cuota_x_202701", MonthlyDues.documentId("x", at(2027, 1, 1, 0), UTC))
        assertEquals(202611, MonthlyDues.monthKey(NOW, UTC))
    }
}
