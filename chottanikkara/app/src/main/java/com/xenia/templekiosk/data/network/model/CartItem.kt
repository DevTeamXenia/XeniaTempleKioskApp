package com.xenia.templekiosk.data.network.model

data class CartItem(
    val categoryId: String,
    val offeringId: String,
    val subTempleId: String,
    val offeringName: String,
    val amount: Double,
    var personName: String? = null,
    var personStar: String? = null
)
