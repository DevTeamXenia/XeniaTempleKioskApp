package com.xenia.churchkiosk.data.network.model

data class LoginResponse(
    val userId: Int,
    val userName: String,
    val companyId: Int
)
