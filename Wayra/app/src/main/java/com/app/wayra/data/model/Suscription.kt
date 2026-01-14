package com.app.wayra.data.model

import com.google.firebase.firestore.Exclude

data class Subscription(
        @get:Exclude val id: String = "",
        val studentId: String = "",
        val planId: String = "",
        val startDate: Long? = null,
        val active: Boolean = true
)