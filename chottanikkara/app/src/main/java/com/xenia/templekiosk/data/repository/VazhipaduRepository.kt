package com.xenia.templekiosk.data.repository

import com.xenia.templekiosk.data.network.service.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VazhipaduRepository {

    suspend fun generateOfferingCat(userId:Int,companyId:Int,subTempleId: Int) = withContext(
        Dispatchers.IO) {
        ApiClient.apiService.generateOfferingCat(userId,companyId,subTempleId)
    }

    suspend fun generateOffering(userId:Int,companyId:Int,subTempleId: Int,categoryId: Int) = withContext(
        Dispatchers.IO) {
        ApiClient.apiService.generateOffering(userId,companyId,subTempleId,categoryId)
    }

}