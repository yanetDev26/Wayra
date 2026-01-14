package com.app.wayra.data.model

import com.google.firebase.firestore.Exclude

data class Plan (
    @get:Exclude val id: String = "",
    val activityName: String = "",
    val price: Double = 0.0,
    val description: String = ""
)