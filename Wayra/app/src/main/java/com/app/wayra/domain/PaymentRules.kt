package com.app.wayra.domain

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import java.util.TimeZone

/** Mes al que corresponde un pago, relativo al mes actual. */
enum class PaymentPeriod(val monthOffset: Int) {
    LAST_MONTH(-1),
    CURRENT_MONTH(0),
    NEXT_MONTH(1)
}

/** Resultado de validar un monto escrito por el usuario. */
sealed class AmountResult {
    data class Valid(val value: Double) : AmountResult()
    data object Empty : AmountResult()
    data object NotANumber : AmountResult()
    data object NotPositive : AmountResult()

    val errorMessage: String?
        get() = when (this) {
            is Valid -> null
            Empty -> "Por favor ingresa un monto"
            NotANumber -> "El monto debe ser un número válido"
            NotPositive -> "El monto debe ser mayor a 0"
        }
}

object PaymentRules {

    /** Un pago que todavía se debe, esté o no marcado como vencido. */
    fun isOutstanding(payment: Payment): Boolean =
        payment.status == PaymentStatus.PENDIENTE || payment.status == PaymentStatus.VENCIDO

    fun isOverdue(payment: Payment, now: Long): Boolean {
        val due = payment.dueDate ?: return false
        return isOutstanding(payment) && due < now
    }

    /**
     * Estado a mostrar. La app nunca guarda VENCIDO: un pago pendiente cuya
     * fecha ya pasó se muestra como vencido. Sin esto, el filtro "Vencido" del
     * historial no encontraba nunca nada.
     */
    fun effectiveStatus(payment: Payment, now: Long): PaymentStatus =
        if (payment.status == PaymentStatus.PENDIENTE && isOverdue(payment, now)) PaymentStatus.VENCIDO
        else payment.status

    /** Días completos de atraso; 0 si vence hoy o no está vencido. */
    fun daysOverdue(payment: Payment, now: Long): Int {
        if (!isOverdue(payment, now)) return 0
        return ((now - payment.dueDate!!) / DateRanges.DAY_MS).toInt()
    }

    fun dueDateFor(period: PaymentPeriod, now: Long, zone: TimeZone = TimeZone.getDefault()): Long =
        DateRanges.dueDateOfMonth(now, period.monthOffset, zone)

    /**
     * Interpreta montos como los escribe un usuario argentino:
     * "1500", "1500.50", "1500,50", "1.500", "1.500,50", "$ 1.500".
     */
    fun parseAmount(text: String): Double? {
        val clean = text.trim().removePrefix("$").replace(" ", "")
        if (clean.isEmpty()) return null
        val normalized = when {
            clean.contains(',') && clean.contains('.') -> clean.replace(".", "").replace(',', '.')
            clean.contains(',') -> clean.replace(',', '.')
            THOUSANDS_ONLY.matches(clean) -> clean.replace(".", "")
            else -> clean
        }
        return normalized.toDoubleOrNull()
    }

    fun validateAmount(text: String): AmountResult {
        if (text.isBlank()) return AmountResult.Empty
        val value = parseAmount(text) ?: return AmountResult.NotANumber
        if (value.isNaN() || value.isInfinite()) return AmountResult.NotANumber
        if (value <= 0) return AmountResult.NotPositive
        return AmountResult.Valid(value)
    }

    /**
     * Pago resultante de "Registrar pago". Si el alumno ya tenía una cuota
     * pendiente ese mes, se salda esa cuota en lugar de crear otra.
     */
    fun registeredPayment(
        existingPending: Payment?,
        studentId: String,
        subscriptionId: String?,
        amount: Double,
        method: PaymentMethod,
        notes: String,
        dueDate: Long,
        now: Long
    ): Payment {
        val base = existingPending ?: Payment(studentId = studentId, dueDate = dueDate)
        return base.copy(
            // Las cuotas creadas al asignar plan no guardaban la suscripción;
            // se completa al cobrarlas para que el reporte por plan las cuente.
            subscriptionId = base.subscriptionId.ifBlank { subscriptionId.orEmpty() },
            amount = amount,
            paymentDate = now,
            paymentMethod = method,
            status = PaymentStatus.PAGADO,
            // Una nota vacía no borra la que ya tenía la cuota ("Pago mensual - …").
            notes = notes.ifBlank { base.notes }
        )
    }

    private val THOUSANDS_ONLY = Regex("""^\d{1,3}(\.\d{3})+$""")
}
