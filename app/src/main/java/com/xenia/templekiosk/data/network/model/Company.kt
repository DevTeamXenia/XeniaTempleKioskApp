package com.xenia.templekiosk.data.network.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Company(
    @SerializedName("CompanyID") val companyId: Int,
    @SerializedName("CompanyName") val companyName: String,
    @SerializedName("LicenseKey") val licenseKey: String,
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Country") val country: String,
    @SerializedName("Address") val address: String,
    @SerializedName("Email") val email: String,
    @SerializedName("Validity") val validity: Date,
    @SerializedName("IsExpired") val isExpired: Boolean
)
