package com.app.wayra.data.model

import com.google.firebase.Timestamp

data class Student (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val registrationDate: Timestamp? = null,
    val phone: String = "",
    val active: Boolean = true
)