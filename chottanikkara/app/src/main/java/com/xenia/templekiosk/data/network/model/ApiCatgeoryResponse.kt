package com.xenia.templekiosk.data.network.model

import com.google.gson.annotations.SerializedName

data class ApiCategoryResponse(
    @SerializedName("Status") val status: String,
    @SerializedName("Data") val data: List<Category>?,
    @SerializedName("Message") val message: String?,
    @SerializedName("Type") val type: Int
)

data class Category(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryNameMa") val categoryNameMa: String?,
    @SerializedName("categoryNameTa") val categoryNameTa: String?,
    @SerializedName("categoryNameTe") val categoryNameTe: String?,
    @SerializedName("categoryNameKa") val categoryNameKa: String?,
    @SerializedName("categoryNameHi") val categoryNameHi: String?,
    @SerializedName("categoryCompanyId") val categoryCompanyId: Int,
    @SerializedName("categoryCreatedDate") val categoryCreatedDate: String,
    @SerializedName("CategoryCreatedBy") val categoryCreatedBy: Int,
    @SerializedName("CategoryModifiedDate") val categoryModifiedDate: String,
    @SerializedName("CategoryModifiedBy") val categoryModifiedBy: Int,
    @SerializedName("CatgeoryActive") val categoryActive: Boolean
)
