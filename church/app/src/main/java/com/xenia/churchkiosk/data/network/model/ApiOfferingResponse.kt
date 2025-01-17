package com.xenia.churchkiosk.data.network.model

import com.google.gson.annotations.SerializedName

data class ApiProductResponse(
    @SerializedName("Status") val status: String,
    @SerializedName("Data") val data: List<Offering>?,
    @SerializedName("Message") val message: String?,
    @SerializedName("Type") val type: Int
)


data class Offering(
    @SerializedName("offeringsId") val offeringsId: Int,
    @SerializedName("offeringsName") val offeringsName: String,
    @SerializedName("offeringsNameMa") val offeringsNameMa: String,
    @SerializedName("offeringsNameTa") val offeringsNameTa: String,
    @SerializedName("offeringsNameTe") val offeringsNameTe: String,
    @SerializedName("offeringsNameKa") val offeringsNameKa: String,
    @SerializedName("offeringsNameHi") val offeringsNameHi: String,
    @SerializedName("offeringsCategoryId") val offeringsCategoryId: Int,
    @SerializedName("offeringsAmount") val offeringsAmount: Double,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryNameHi") val categoryNameHi: String,
    @SerializedName("categoryNameKa") val categoryNameKa: String,
    @SerializedName("categoryNameMa") val categoryNameMa: String,
    @SerializedName("categoryNameTa") val categoryNameTa: String,
    @SerializedName("categoryNameTe") val categoryNameTe: String,
    @SerializedName("offeringsCreatedDate") val offeringsCreatedDate: String,
    @SerializedName("offeringsCreatedBy") val offeringsCreatedBy: Int,
    @SerializedName("offeringsModifiedDate") val offeringsModifiedDate: String,
    @SerializedName("offeringsModifiedBy") val offeringsModifiedBy: Int,
    @SerializedName("offeringsActive") val offeringsActive: Boolean
)

