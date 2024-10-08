package com.xenia.templekiosk.data.network.model

data class PaymentResponse(
    val Status: String,
    val Data: Data?,
    val Message: String?,
    val Type: Int
)

data class Data(
    val AccessToken: String,
    val IntentUrl: String,
    val pspRefNo: String,
    val status: String,
    val statusDesc: String,
)

