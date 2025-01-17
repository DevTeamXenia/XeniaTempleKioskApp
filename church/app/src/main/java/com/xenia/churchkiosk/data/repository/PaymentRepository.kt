package com.xenia.churchkiosk.data.repository


import com.xenia.churchkiosk.data.network.model.OrderRequest
import com.xenia.churchkiosk.data.network.model.PaymentRequest
import com.xenia.churchkiosk.data.network.model.PaymentStatus
import com.xenia.churchkiosk.data.network.model.TK_Vazhipadi
import com.xenia.churchkiosk.data.network.service.ApiClient
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

    suspend fun postOrder(userId:Int,companyId:Int,request: OrderRequest) = withContext(Dispatchers.IO) {
        ApiClient.apiService.postOrder(userId,companyId,request)
    }

    suspend fun postVazhipadu(userId: Int, companyId: Int, orderRequest: TK_Vazhipadi) = withContext(Dispatchers.IO) {
        ApiClient.apiService.postVazhipadi(userId,companyId,orderRequest)
    }

}