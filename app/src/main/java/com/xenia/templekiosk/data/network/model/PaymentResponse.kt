package com.xenia.templekiosk.data.network.model

data class PaymentResponse(
    val status: String,
    val data: Data?,
    val message: String?,
    val type: Int
)

data class Data(
    val accessToken: String,
    val intentUrl: String,
    val pspRefNo: String
)

