package com.xenia.churchkiosk.data.repository

import com.xenia.churchkiosk.data.network.service.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository {
    suspend fun login(userId:String,password:String) = withContext(Dispatchers.IO) {
        ApiClient.apiService.login(userId,password)
    }

    suspend fun getCompany(companyId:Int) = withContext(Dispatchers.IO) {
        ApiClient.apiService.getCompany(companyId)
    }
}