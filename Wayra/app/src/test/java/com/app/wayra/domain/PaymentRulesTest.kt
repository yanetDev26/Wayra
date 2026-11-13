package com.app.wayra.domain

import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.domain.TestData.NOW
import com.app.wayra.domain.TestData.UTC
import com.app.wayra.domain.TestData.at
import com.app.wayra.domain.TestData.paid
import com.app.wayra.domain.TestData.pending
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentRulesTest {

    // ---------------- Estados

    @Test
    fun `pendiente y vencido se deben, pagado no`() {
        assertTrue(PaymentRules.isOutstanding(pending(100.0, NOW)))
        assertTrue(PaymentRules.isOutstanding(pending(100.0, NOW, status = PaymentStatus.VENCIDO)))
        assertFalse(PaymentRules.isOutstanding(paid(100.0, NOW)))
    }

    @Test
    fun `una cuota pendiente esta vencida solo si su fecha ya paso`() {
        assertTrue(PaymentRules.isOverdue(pending(100.0, NOW - 1), NOW))
        assertFalse(PaymentRules.isOverdue(pending(100.0, NOW), NOW))
        assertFalse(PaymentRules.isOverdue(pending(100.0, NOW + 1), NOW))
        assertFalse(PaymentRules.isOverdue(pending(100.0, null), NOW))
    }

    @Test
    fun `un pago cobrado nunca esta vencido aunque su vencimiento haya pasado`() {
        assertFalse(PaymentRules.isOverdue(paid(100.0, NOW, dueDate = NOW - 10 * DateRanges.DAY_MS), NOW))
    }

    @Test
    fun `el estado efectivo muestra como vencida una cuota pendiente atrasada`() {
        assertEquals(PaymentStatus.VENCIDO, PaymentRules.effectiveStatus(pending(100.0, NOW - 1), NOW))
        assertEquals(PaymentStatus.PENDIENTE, PaymentRules.effectiveStatus(pending(100.0, NOW + 1), NOW))
        assertEquals(PaymentStatus.PAGADO, PaymentRules.effectiveStatus(paid(100.0, NOW), NOW))
    }

    @Test
    fun `dias de atraso cuenta dias completos`() {
        assertEquals(0, PaymentRules.daysOverdue(pending(100.0, NOW - 1000), NOW))
        assertEquals(1, PaymentRules.daysOverdue(pending(100.0, NOW - DateRanges.DAY_MS), NOW))
        assertEquals(13, PaymentRules.daysOverdue(pending(100.0, at(2026, 10, 31, 12)), NOW))
        assertEquals(0, PaymentRules.daysOverdue(pending(100.0, NOW + DateRanges.DAY_MS), NOW))
    }

    // ---------------- Periodo del pago

    @Test
    fun `el periodo elegido define el vencimiento`() {
        assertEquals(at(2026, 10, 31, 23, 59, 59), PaymentRules.dueDateFor(PaymentPeriod.LAST_MONTH, NOW, UTC))
        assertEquals(at(2026, 11, 30, 23, 59, 59), PaymentRules.dueDateFor(PaymentPeriod.CURRENT_MONTH, NOW, UTC))
        assertEquals(at(2026, 12, 31, 23, 59, 59), PaymentRules.dueDateFor(PaymentPeriod.NEXT_MONTH, NOW, UTC))
    }

    // ---------------- Montos

    @Test
    fun `interpreta montos escritos a la argentina`() {
        assertEquals(1500.0, PaymentRules.parseAmount("1500")!!, 0.0)
        assertEquals(1500.5, PaymentRules.parseAmount("1500.50")!!, 0.0)
        assertEquals(1500.5, PaymentRules.parseAmount("1500,50")!!, 0.0)
        assertEquals(1500.0, PaymentRules.parseAmount("1.500")!!, 0.0)
        assertEquals(1234567.0, PaymentRules.parseAmount("1.234.567")!!, 0.0)
        assertEquals(1500.5, PaymentRules.parseAmount("1.500,50")!!, 0.0)
        assertEquals(1500.0, PaymentRules.parseAmount(" $ 1.500 ")!!, 0.0)
    }

    @Test
    fun `acepta el monto que la app precarga desde el precio del plan`() {
        // El formulario recibe plan.price.toString(), por ejemplo "15000.0".
        assertEquals(15000.0, PaymentRules.parseAmount(15000.0.toString())!!, 0.0)
    }

    @Test
    fun `texto que no es numero no es un monto`() {
        assertNull(PaymentRules.parseAmount(""))
        assertNull(PaymentRules.parseAmount("abc"))
        assertNull(PaymentRules.parseAmount("12a"))
    }

    @Test
    fun `validacion de monto con su mensaje`() {
        assertEquals(AmountResult.Empty, PaymentRules.validateAmount("  "))
        assertEquals(AmountResult.NotANumber, PaymentRules.validateAmount("abc"))
        assertEquals(AmountResult.NotPositive, PaymentRules.validateAmount("0"))
        assertEquals(AmountResult.NotPositive, PaymentRules.validateAmount("-10"))
        assertEquals(AmountResult.Valid(2500.0), PaymentRules.validateAmount("2.500"))
        assertEquals("Por favor ingresa un monto", AmountResult.Empty.errorMessage)
        assertNull(AmountResult.Valid(1.0).errorMessage)
    }

    // ---------------- Registrar pago

    private val due = at(2026, 11, 30, 23, 59, 59)

    @Test
    fun `sin cuota pendiente se crea un pago nuevo cobrado`() {
        val p = PaymentRules.registeredPayment(
            existingPending = null, studentId = "s1", subscriptionId = "sub1", amount = 1000.0,
            method = PaymentMethod.TRANSFERENCIA, notes = "", dueDate = due, now = NOW
        )
        assertEquals("", p.id)
        assertEquals("s1", p.studentId)
        assertEquals("sub1", p.subscriptionId)
        assertEquals(1000.0, p.amount, 0.0)
        assertEquals(due, p.dueDate)
        assertEquals(NOW, p.paymentDate)
        assertEquals(PaymentMethod.TRANSFERENCIA, p.paymentMethod)
        assertEquals(PaymentStatus.PAGADO, p.status)
    }

    @Test
    fun `con cuota pendiente se salda esa cuota y conserva su vencimiento`() {
        val existing = pending(800.0, at(2026, 11, 30, 23, 59, 59, 412), notes = "Pago mensual - Running", id = "q1")
        val p = PaymentRules.registeredPayment(
            existingPending = existing, studentId = "s1", subscriptionId = "sub1", amount = 1000.0,
            method = PaymentMethod.EFECTIVO, notes = "", dueDate = due, now = NOW
        )
        assertEquals("q1", p.id)
        assertEquals(existing.dueDate, p.dueDate)
        assertEquals(1000.0, p.amount, 0.0)
        assertEquals(PaymentStatus.PAGADO, p.status)
        assertEquals(NOW, p.paymentDate)
    }

    @Test
    fun `una nota vacia no borra la nota de la cuota`() {
        val existing = pending(800.0, due, notes = "Pago mensual - Running")
        val keep = PaymentRules.registeredPayment(existing, "s1", null, 800.0, PaymentMethod.EFECTIVO, "  ", due, NOW)
        assertEquals("Pago mensual - Running", keep.notes)

        val replace = PaymentRules.registeredPayment(existing, "s1", null, 800.0, PaymentMethod.EFECTIVO, "Pagó en mano", due, NOW)
        assertEquals("Pagó en mano", replace.notes)
    }

    @Test
    fun `al cobrar una cuota sin suscripcion se le asigna la actual`() {
        val withoutSub = pending(800.0, due)
        assertEquals("sub1", PaymentRules.registeredPayment(withoutSub, "s1", "sub1", 800.0, PaymentMethod.EFECTIVO, "", due, NOW).subscriptionId)

        val withSub = pending(800.0, due, subscriptionId = "old")
        assertEquals("old", PaymentRules.registeredPayment(withSub, "s1", "sub1", 800.0, PaymentMethod.EFECTIVO, "", due, NOW).subscriptionId)

        assertEquals("", PaymentRules.registeredPayment(null, "s1", null, 800.0, PaymentMethod.EFECTIVO, "", due, NOW).subscriptionId)
    }
}
