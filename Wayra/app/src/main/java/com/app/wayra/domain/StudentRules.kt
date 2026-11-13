package com.app.wayra.domain

import com.app.wayra.data.model.Student
import java.text.Normalizer

/** Búsqueda de alumnos y validación de sus datos. */
object StudentRules {

    /** Minúsculas, sin acentos y con espacios simples: "  José  Núñez " -> "jose nunez". */
    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .replace(SPACES, " ")
            .trim()

    /**
     * Cada palabra buscada tiene que aparecer en algún dato del alumno, en
     * cualquier orden: "perez juan" encuentra a "Juan Pérez". Si la palabra
     * tiene dígitos también se compara contra el teléfono ignorando guiones y
     * espacios. Una búsqueda vacía coincide con todos.
     */
    fun matchesSearch(student: Student, planName: String?, query: String): Boolean {
        val tokens = normalize(query).split(' ').filter { it.isNotBlank() }
        if (tokens.isEmpty()) return true
        val text = normalize(
            listOf(student.name, student.surname, student.email, planName.orEmpty()).joinToString(" ")
        )
        val phoneDigits = student.phone.filter { it.isDigit() }
        return tokens.all { token ->
            text.contains(token) ||
                (token.any { it.isDigit() } && phoneDigits.contains(token.filter { it.isDigit() }))
        }
    }

    fun isValidEmail(email: String): Boolean = EMAIL.matches(email.trim())

    /** Celular argentino: 10 dígitos (código de área + número, sin 0 ni 15). */
    fun isValidPhone(phone: String): Boolean = phone.count { it.isDigit() } == 10

    /** "1155551234" -> "1155 551234", tal como se muestra mientras se escribe. */
    fun formatPhone(phone: String): String = when {
        phone.isEmpty() -> ""
        phone.length <= 4 -> phone
        phone.length <= 10 -> "${phone.substring(0, 4)} ${phone.substring(4)}"
        else -> phone
    }

    /** Los datos obligatorios para dar de alta un alumno. */
    fun canCreateStudent(
        name: String,
        surname: String,
        email: String,
        phone: String,
        emergencyPhone: String,
        hasPlan: Boolean
    ): Boolean =
        name.isNotBlank() &&
            surname.isNotBlank() &&
            isValidEmail(email) &&
            isValidPhone(phone) &&
            (emergencyPhone.isBlank() || isValidPhone(emergencyPhone)) &&
            hasPlan

    private val DIACRITICS = Regex("\\p{M}+")
    private val SPACES = Regex("\\s+")
    private val EMAIL = Regex("""^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$""")
}

/** Validación del formulario de planes. */
object PlanRules {

    /** Precio válido o null. Acepta coma decimal y punto de miles ("1.500,50"). */
    fun parsePrice(text: String): Double? =
        (PaymentRules.validateAmount(text) as? AmountResult.Valid)?.value

    fun canSave(activityName: String, priceText: String): Boolean =
        activityName.isNotBlank() && parsePrice(priceText) != null
}
