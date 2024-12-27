package com.xeniatechnologies.app.nimahal.data.repository

import com.xeniatechnologies.app.nimahal.data.network.service.ApiClient
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentRequest
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository {
    suspend fun generateToken(userId:Int,companyId:Int) = withContext(Dispatchers.IO) {
        ApiClient.apiService.generateToken(userId,companyId)
    }

    suspend fun generateQr(userId:Int,companyId:Int,request: PaymentRequest) = withContext(Dispatchers.IO) {
        ApiClient.apiService.generateQr(userId,companyId,request)
    }

    suspend fun paymentStatus(userId:Int,companyId:Int,request: PaymentStatus) = withContext(Dispatchers.IO) {
        ApiClient.apiService.paymentStatus(userId,companyId,request)
    }


}