package com.xenia.churchkiosk.data.network.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CompanyResponse(
    @SerializedName("Status")
    val status: String,
    @SerializedName("Data")
    val data: com.xenia.churchkiosk.data.network.model.Company,
    val message: String?,
    val type: Int
)

data class Company(
    val companyId: Int,
    val companyName: String,
    val companyAddress: String,
    val companyPhone1: String,
    val companyPhone2: String,
    val companyRegNo1: String,
    val companyRegNo2: String,
    val stateName: String,
    val companyDefaultLanguage: String,
    val isCategoryEnable: Boolean,
)
