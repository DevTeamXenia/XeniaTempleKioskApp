package com.xenia.templekiosk.data.network.model

data class PersonDevathaOffering(
    val personName: String,
    val devatha: String,
    val offerings: List<PersonWithItems>
)