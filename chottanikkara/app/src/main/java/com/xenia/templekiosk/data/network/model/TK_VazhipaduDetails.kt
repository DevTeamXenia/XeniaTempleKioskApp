package com.xenia.templekiosk.data.network.model

data class TK_VazhipaduDetails(
    val vaId: Int,
    val vaName: String,
    val vaPhoneNumber: String,
    val vaOfferingsId: Int,
    val vaOfferingsAmount: Double,
    val vaSubTempleId: Int
)