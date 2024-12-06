package com.xenia.templekiosk.data.network.model

data class PersonWithItems(
    val personName: String,
    val personStar: String,
    val items: List<CartItem>
)
