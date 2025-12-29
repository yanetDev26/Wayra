package com.app.wayra.data.model

data class Subscription(
        val id: String = "",
        val studentId: String = "",
        val planId: String = "",
        val startDate: Long? = null,
        val active: Boolean = true
)