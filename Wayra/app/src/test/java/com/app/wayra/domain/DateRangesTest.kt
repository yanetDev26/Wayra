package com.app.wayra.domain

import com.app.wayra.domain.TestData.UTC
import com.app.wayra.domain.TestData.at
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateRangesTest {

    @Test
    fun `el mes actual va del dia 1 a las 0 hasta el ultimo milisegundo del mes`() {
        val range = DateRanges.month(at(2026, 11, 13), 0, UTC)
        assertEquals(at(2026, 11, 1, 0), range.start)
        assertEquals(at(2026, 11, 30, 23, 59, 59, 999), range.end)
    }

    @Test
    fun `el mes anterior cruza el cambio de anio`() {
        val range = DateRanges.month(at(2026, 1, 15), -1, UTC)
        assertEquals(at(2025, 12, 1, 0), range.start)
        assertEquals(at(2025, 12, 31, 23, 59, 59, 999), range.end)
    }

    @Test
    fun `restar un mes al 31 de marzo da febrero, no marzo`() {
        val range = DateRanges.month(at(2026, 3, 31), -1, UTC)
        assertEquals(at(2026, 2, 1, 0), range.start)
        assertEquals(at(2026, 2, 28, 23, 59, 59, 999), range.end)
    }

    @Test
    fun `febrero de anio bisiesto tiene 29 dias`() {
        val range = DateRanges.month(at(2028, 2, 10), 0, UTC)
        assertEquals(at(2028, 2, 29, 23, 59, 59, 999), range.end)
    }

    @Test
    fun `un vencimiento con milisegundos al final del mes queda dentro del rango`() {
        // Bug anterior: el fin de mes se calculaba con los ms del momento.
        val range = DateRanges.month(at(2026, 11, 13), 0, UTC)
        assertTrue(at(2026, 11, 30, 23, 59, 59, 999) in range)
        assertFalse(at(2026, 12, 1, 0) in range)
        assertFalse(at(2026, 10, 31, 23, 59, 59, 999) in range)
    }

    @Test
    fun `el vencimiento estandar es el ultimo dia del mes a las 23 59 59`() {
        assertEquals(at(2026, 11, 30, 23, 59, 59), DateRanges.dueDateOfMonth(at(2026, 11, 13), 0, UTC))
        assertEquals(at(2026, 10, 31, 23, 59, 59), DateRanges.dueDateOfMonth(at(2026, 11, 13), -1, UTC))
        assertEquals(at(2026, 12, 31, 23, 59, 59), DateRanges.dueDateOfMonth(at(2026, 11, 13), 1, UTC))
        assertEquals(at(2027, 1, 31, 23, 59, 59), DateRanges.dueDateOfMonth(at(2026, 12, 31), 1, UTC))
    }

    @Test
    fun `nombres de mes en castellano`() {
        assertEquals("Noviembre 2026", DateRanges.monthLabel(at(2026, 11, 13), UTC))
        assertEquals("Enero", DateRanges.monthName(at(2027, 1, 1), UTC))
        assertEquals(2027, DateRanges.year(at(2027, 1, 1), UTC))
    }
}
