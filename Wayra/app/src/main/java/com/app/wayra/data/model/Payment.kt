package com.app.wayra.data.model

import com.google.firebase.firestore.Exclude

enum class PaymentMethod {
    EFECTIVO,
    TRANSFERENCIA
}

enum class PaymentStatus {
    PENDIENTE,
    PAGADO,
    VENCIDO
}

data class Payment(
    @get:Exclude val id: String = "",
    val studentId: String = "",
    val subscriptionId: String = "",
    val amount: Double = 0.0,
    val dueDate: Long? = null,
    val paymentDate: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val status: PaymentStatus = PaymentStatus.PENDIENTE,
    val notes: String = ""
)
