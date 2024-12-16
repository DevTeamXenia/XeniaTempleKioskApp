package com.xenia.templekiosk.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Vazhipadu")
data class Vazhipadu(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val vaName: String,
    val vaPhoneNumber: String,
    val vaStar: String,
    val vaStarLa: String,
    val vaOfferingsId: Int,
    val vaOfferingsName: String,
    val vaOfferingsNameMa: String,
    val vaOfferingsNameTa: String,
    val vaOfferingsNameKa: String,
    val vaOfferingsNameTe: String,
    val vaOfferingsNameHi: String,
    val vaOfferingsAmount: Double,
    val vaSubTempleId: Int,
    val vaSubTempleName: String,
    val vaIsCompleted: Boolean,
)
