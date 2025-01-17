package com.xenia.churchkiosk.data.network.model

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("Status")
    val status: String,
    @SerializedName("Data")
    val data: OrderData,
    val message: String?,
    val type: Int
)

data class OrderData(
    @SerializedName("OrderId")
    val orderId: Int,
)
