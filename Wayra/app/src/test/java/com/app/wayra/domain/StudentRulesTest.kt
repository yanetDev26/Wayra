package com.app.wayra.domain

import com.app.wayra.domain.TestData.student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentRulesTest {

    private val juan = student("1", name = "Juan", surname = "Pérez", email = "juan.perez@mail.com", phone = "11-5555-1234")
    private val maria = student("2", name = "María José", surname = "Núñez", email = "mj@mail.com", phone = "1144443333")

    // ---------------- Búsqueda

    @Test
    fun `busqueda vacia o con espacios encuentra a todos`() {
        assertTrue(StudentRules.matchesSearch(juan, null, ""))
        assertTrue(StudentRules.matchesSearch(juan, null, "   "))
    }

    @Test
    fun `ignora mayusculas y acentos en los dos sentidos`() {
        assertTrue(StudentRules.matchesSearch(juan, null, "perez"))
        assertTrue(StudentRules.matchesSearch(juan, null, "PÉREZ"))
        assertTrue(StudentRules.matchesSearch(maria, null, "nunez"))
        assertTrue(StudentRules.matchesSearch(maria, null, "maria jose"))
    }

    @Test
    fun `las palabras pueden ir en cualquier orden`() {
        assertTrue(StudentRules.matchesSearch(juan, null, "perez juan"))
        assertTrue(StudentRules.matchesSearch(juan, null, "  juan   per "))
    }

    @Test
    fun `todas las palabras tienen que coincidir`() {
        assertFalse(StudentRules.matchesSearch(juan, null, "juan gomez"))
    }

    @Test
    fun `busca por email y por nombre del plan`() {
        assertTrue(StudentRules.matchesSearch(juan, null, "juan.perez@"))
        assertTrue(StudentRules.matchesSearch(juan, "Running Avanzado", "running"))
        assertFalse(StudentRules.matchesSearch(juan, null, "running"))
    }

    @Test
    fun `busca telefono ignorando guiones y espacios`() {
        assertTrue(StudentRules.matchesSearch(juan, null, "1155551234"))
        assertTrue(StudentRules.matchesSearch(juan, null, "5555-12"))
        assertTrue(StudentRules.matchesSearch(maria, null, "4444 3333"))
        assertFalse(StudentRules.matchesSearch(juan, null, "9999"))
    }

    @Test
    fun `normaliza texto`() {
        assertEquals("jose nunez", StudentRules.normalize("  José   NÚÑEZ "))
    }

    // ---------------- Validaciones

    @Test
    fun `email valido e invalido`() {
        assertTrue(StudentRules.isValidEmail("ana@gmail.com"))
        assertTrue(StudentRules.isValidEmail(" ana.b+run@mail.com.ar "))
        assertFalse(StudentRules.isValidEmail(""))
        assertFalse(StudentRules.isValidEmail("ana"))
        assertFalse(StudentRules.isValidEmail("ana@"))
        assertFalse(StudentRules.isValidEmail("ana@gmail"))
        assertFalse(StudentRules.isValidEmail("ana @gmail.com"))
    }

    @Test
    fun `celular argentino tiene 10 digitos`() {
        assertTrue(StudentRules.isValidPhone("1155551234"))
        assertTrue(StudentRules.isValidPhone("1155 551234"))
        assertFalse(StudentRules.isValidPhone("115555123"))
        assertFalse(StudentRules.isValidPhone("11555512345"))
        assertFalse(StudentRules.isValidPhone(""))
    }

    @Test
    fun `formato del telefono mientras se escribe`() {
        assertEquals("", StudentRules.formatPhone(""))
        assertEquals("115", StudentRules.formatPhone("115"))
        assertEquals("1155", StudentRules.formatPhone("1155"))
        assertEquals("1155 5", StudentRules.formatPhone("11555"))
        assertEquals("1155 551234", StudentRules.formatPhone("1155551234"))
    }

    @Test
    fun `alta de alumno exige nombre, apellido, email, celular y plan`() {
        fun can(
            name: String = "Juan", surname: String = "Pérez", email: String = "j@p.com",
            phone: String = "1155551234", emergency: String = "", hasPlan: Boolean = true
        ) = StudentRules.canCreateStudent(name, surname, email, phone, emergency, hasPlan)

        assertTrue(can())
        assertTrue(can(emergency = "1144443333"))
        assertFalse(can(name = " "))
        assertFalse(can(surname = ""))
        assertFalse(can(email = "mal"))
        assertFalse(can(phone = "123"))
        assertFalse(can(emergency = "123"))
        assertFalse(can(hasPlan = false))
    }

    // ---------------- Planes

    @Test
    fun `precio del plan acepta coma decimal y rechaza texto o cero`() {
        assertEquals(15000.5, PlanRules.parsePrice("15.000,50")!!, 0.0)
        assertNull(PlanRules.parsePrice("abc"))
        assertNull(PlanRules.parsePrice("0"))
        assertNull(PlanRules.parsePrice(""))
    }

    @Test
    fun `un plan se puede guardar con nombre y precio valido`() {
        assertTrue(PlanRules.canSave("Running", "12000"))
        assertFalse(PlanRules.canSave("", "12000"))
        assertFalse(PlanRules.canSave("Running", "gratis"))
    }
}
