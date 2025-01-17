package com.xenia.churchkiosk.data.network.model

import com.google.gson.annotations.SerializedName

data class OrderVazhipaduResponse(
    @SerializedName("Status")
    val status: String,
    @SerializedName("Data")
    val data: OrderVazhipaduData,
    val message: String?,
    val type: Int
)

data class OrderVazhipaduData(
    @SerializedName("OrderId")
    val orderId: Int,
)
