package com.xenia.templekiosk.data.network.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("TransactionId")
    val transactionId: String,

    @SerializedName("Devatha")
    val devatha: String,

    @SerializedName("Nakshatra")
    val nakshatra: String,

    @SerializedName("Name")
    val name: String,

    @SerializedName("PhoneNumber")
    val phoneNumber: String,

    @SerializedName("OrderAmount")
    val orderAmount: Double,

    @SerializedName("PaymentStatus")
    val paymentStatus: String,

    @SerializedName("PaymentMethod")
    val paymentMethod: String
)
