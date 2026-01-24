package com.app.wayra.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Student (
    @get:Exclude val id: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val registrationDate: Timestamp? = null,
    val phone: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val active: Boolean = true
) {
    fun getFullName(): String = "$name $surname".trim()
}