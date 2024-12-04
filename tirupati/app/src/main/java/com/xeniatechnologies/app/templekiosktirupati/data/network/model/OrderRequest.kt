package com.xeniatechnologies.app.templekiosktirupati.data.network.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("TransactionId")
    val transactionId: String,

    @SerializedName("PhoneNumber")
    val phoneNumber: String,

    @SerializedName("OrderAmount")
    val orderAmount: Double,

    @SerializedName("PaymentStatus")
    val paymentStatus: String,

    @SerializedName("PaymentDes")
    val paymentDes: String,

    @SerializedName("PaymentMethod")
    val paymentMethod: String
)
