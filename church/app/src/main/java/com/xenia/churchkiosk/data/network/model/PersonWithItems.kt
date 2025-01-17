package com.xenia.churchkiosk.data.network.model


data class PersonWithItems(
    val personName: String,
    val items: List<OfferingItem>
)
