package com.xenia.templekiosk.data.repository

import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.network.service.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository {
    suspend fun generateToken(mid:String ) = withContext(Dispatchers.IO) {
        ApiClient.apiService.generateToken(mid)
    }

    suspend fun generateQr(mid:String,request: PaymentRequest) = withContext(Dispatchers.IO) {
        ApiClient.apiService.generateQr(mid,request)
    }
}