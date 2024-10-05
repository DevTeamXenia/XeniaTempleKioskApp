package com.xenia.templekiosk.data.network.model

data class PaymentRequest (
    val acessToken: String,
    val transactionReferenceID: String,
    val amount: String
)
