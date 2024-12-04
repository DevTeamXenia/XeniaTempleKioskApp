package com.xeniatechnologies.app.templekiosktirupati.data.network.model

data class PaymentRequest (
    val acessToken: String,
    val transactionReferenceID: String,
    val amount: String,
    val phoneNumber: String,
)
