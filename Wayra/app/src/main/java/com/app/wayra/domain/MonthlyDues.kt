package com.app.wayra.domain

import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import java.util.Calendar
import java.util.TimeZone

/** Una cuota a crear, con el id de documento con que se guarda. */
data class NewDue(val documentId: String, val payment: Payment)

/**
 * Generación de la cuota mensual.
 *
 * Antes la cuota pendiente solo se creaba al asignar o cambiar un plan, y solo
 * para ese mes: desde el segundo mes de cada alumno no había nada que cobrar
 * y Inicio y Reportes mostraban "sin deudas".
 */
object MonthlyDues {

    /**
     * Cuotas que faltan en el mes de [now]: una por alumno activo con plan
     * vigente que todavía no tenga ningún pago (pendiente o cobrado) con
     * vencimiento en ese mes. Así no se duplica la cuota de quien ya pagó, de
     * quien pagó por adelantado ni la que se creó al asignar el plan.
     */
    fun missingDues(
        students: List<Student>,
        subscriptions: List<Subscription>,
        plans: List<Plan>,
        payments: List<Payment>,
        now: Long,
        zone: TimeZone = TimeZone.getDefault()
    ): List<NewDue> {
        val month = DateRanges.month(now, 0, zone)
        val dueDate = DateRanges.dueDateOfMonth(now, 0, zone)
        val activeIds = students.filter { it.active }.map { it.id }.toSet()
        val plansById = plans.associateBy { it.id }
        val alreadyHasPayment = payments
            .filter { it.dueDate != null && it.dueDate in month }
            .map { it.studentId }
            .toSet()

        return ReportCalculator.currentSubscriptions(subscriptions, activeIds)
            .filter { it.studentId !in alreadyHasPayment }
            // Una suscripción que arranca después de este mes todavía no se cobra.
            .filter { (it.startDate ?: 0L) <= month.end }
            .mapNotNull { sub ->
                val plan = plansById[sub.planId] ?: return@mapNotNull null
                NewDue(
                    documentId = documentId(sub.studentId, now, zone),
                    payment = Payment(
                        studentId = sub.studentId,
                        subscriptionId = sub.id,
                        amount = plan.price,
                        dueDate = dueDate,
                        status = PaymentStatus.PENDIENTE,
                        notes = "Pago mensual - ${plan.activityName}"
                    )
                )
            }
            .sortedBy { it.documentId }
    }

    /**
     * Id fijo por alumno y mes ("cuota_<alumno>_202611"). Si dos teléfonos
     * generan la misma cuota a la vez, la segunda escritura pisa a la primera
     * en lugar de crear un duplicado.
     */
    fun documentId(studentId: String, time: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(zone).apply { timeInMillis = time }
        val yyyymm = c.get(Calendar.YEAR) * 100 + (c.get(Calendar.MONTH) + 1)
        return "cuota_${studentId}_$yyyymm"
    }

    /** Clave del mes, para no generar dos veces en la misma sesión. */
    fun monthKey(time: Long, zone: TimeZone = TimeZone.getDefault()): Int {
        val c = Calendar.getInstance(zone).apply { timeInMillis = time }
        return c.get(Calendar.YEAR) * 100 + (c.get(Calendar.MONTH) + 1)
    }
}
