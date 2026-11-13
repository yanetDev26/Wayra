package com.app.wayra.domain

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import java.util.Calendar
import java.util.TimeZone

/** Datos y fechas fijas para los tests: todo en UTC para que no dependan de la máquina. */
object TestData {

    val UTC: TimeZone = TimeZone.getTimeZone("UTC")

    /** Milisegundos de una fecha en UTC. `month` es 1..12. */
    fun at(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0, second: Int = 0, ms: Int = 0): Long =
        Calendar.getInstance(UTC).apply {
            clear()
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, ms)
        }.timeInMillis

    /** "Hoy" en todos los tests: 13 de noviembre de 2026, mediodía. */
    val NOW = at(2026, 11, 13)
    val NOVEMBER = DateRanges.month(NOW, 0, UTC)
    val OCTOBER = DateRanges.month(NOW, -1, UTC)

    fun paid(
        amount: Double,
        paidAt: Long,
        studentId: String = "s1",
        method: PaymentMethod? = PaymentMethod.EFECTIVO,
        subscriptionId: String = "",
        dueDate: Long? = null,
        id: String = "p-$studentId-$paidAt"
    ) = Payment(
        id = id,
        studentId = studentId,
        subscriptionId = subscriptionId,
        amount = amount,
        dueDate = dueDate,
        paymentDate = paidAt,
        paymentMethod = method,
        status = PaymentStatus.PAGADO
    )

    fun pending(
        amount: Double,
        dueDate: Long?,
        studentId: String = "s1",
        status: PaymentStatus = PaymentStatus.PENDIENTE,
        subscriptionId: String = "",
        notes: String = "",
        id: String = "q-$studentId-$dueDate"
    ) = Payment(
        id = id,
        studentId = studentId,
        subscriptionId = subscriptionId,
        amount = amount,
        dueDate = dueDate,
        status = status,
        notes = notes
    )

    fun student(id: String, name: String = "Nombre", surname: String = "Apellido", active: Boolean = true,
                email: String = "", phone: String = "") =
        Student(id = id, name = name, surname = surname, active = active, email = email, phone = phone)

    fun plan(id: String, name: String, price: Double) = Plan(id = id, activityName = name, price = price)

    fun subscription(id: String, studentId: String, planId: String, startDate: Long?, active: Boolean = true) =
        Subscription(id = id, studentId = studentId, planId = planId, startDate = startDate, active = active)
}
