package com.xenia.templekiosk.data.network.model

data class TK_Vazhipadi(
    val vTranscationId: String,
    val vTotalAmount: Double,
    val vPaymentStatus: String,
    val vPaymentDes: String,
    val TK_VazhipaduDetails: List<TK_VazhipaduDetails>
)
