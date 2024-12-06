package com.xenia.templekiosk.data.network.model

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
    @SerializedName("offeringsCategoryId") val offeringsCategoryId: Int,
    @SerializedName("offeringsAmount") val offeringsAmount: Double,
    @SerializedName("offeringsCreatedDate") val offeringsCreatedDate: String,
    @SerializedName("offeringsCreatedBy") val offeringsCreatedBy: Int,
    @SerializedName("offeringsModifiedDate") val offeringsModifiedDate: String,
    @SerializedName("offeringsModifiedBy") val offeringsModifiedBy: Int,
    @SerializedName("offeringsActive") val offeringsActive: Boolean
)

