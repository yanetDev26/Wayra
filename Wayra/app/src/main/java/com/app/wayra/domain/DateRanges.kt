package com.app.wayra.domain

import java.util.Calendar
import java.util.TimeZone

/** Intervalo cerrado [start, end] en milisegundos. */
data class MonthRange(val start: Long, val end: Long) {
    operator fun contains(time: Long): Boolean = time in start..end
}

/**
 * Cálculo de meses calendario.
 *
 * Antes cada pantalla y repositorio armaba su propio rango con `Calendar`, y
 * no todos ponían los milisegundos en cero: un pago con vencimiento a las
 * 23:59:59.800 podía quedar fuera del "fin de mes" calculado a las 23:59:59.300.
 * Acá el fin de mes es "inicio del mes siguiente - 1 ms", que no deja huecos.
 *
 * Todas las funciones reciben `now` y la zona horaria para poder testearlas.
 */
object DateRanges {

    const val DAY_MS: Long = 24L * 60 * 60 * 1000

    private val MONTH_NAMES = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    /** Mes calendario desplazado [offset] meses respecto de [now] (0 = este mes). */
    fun month(now: Long, offset: Int = 0, zone: TimeZone = TimeZone.getDefault()): MonthRange {
        val c = startOfMonthCalendar(now, offset, zone)
        val start = c.timeInMillis
        c.add(Calendar.MONTH, 1)
        return MonthRange(start, c.timeInMillis - 1)
    }

    /**
     * Vencimiento estándar de una cuota: último día del mes a las 23:59:59.
     * Es el formato con el que la app siempre guardó `dueDate`.
     */
    fun dueDateOfMonth(now: Long, offset: Int = 0, zone: TimeZone = TimeZone.getDefault()): Long {
        val c = startOfMonthCalendar(now, offset, zone)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        return c.timeInMillis
    }

    /** "Noviembre 2026" para el mes que contiene [time]. */
    fun monthLabel(time: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(zone).apply { timeInMillis = time }
        return "${MONTH_NAMES[c.get(Calendar.MONTH)]} ${c.get(Calendar.YEAR)}"
    }

    fun monthName(time: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(zone).apply { timeInMillis = time }
        return MONTH_NAMES[c.get(Calendar.MONTH)]
    }

    fun year(time: Long, zone: TimeZone = TimeZone.getDefault()): Int =
        Calendar.getInstance(zone).apply { timeInMillis = time }.get(Calendar.YEAR)

    private fun startOfMonthCalendar(now: Long, offset: Int, zone: TimeZone): Calendar {
        val c = Calendar.getInstance(zone)
        c.timeInMillis = now
        // Primero al día 1 y después se suman meses: así un 31 de marzo menos
        // un mes no se desborda a otro día.
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.MONTH, offset)
        return c
    }
}
