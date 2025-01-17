package com.xenia.churchkiosk.data.network.model

data class PaymentRequest (
    val acessToken: String,
    val transactionReferenceID: String,
    val amount: String,
)
