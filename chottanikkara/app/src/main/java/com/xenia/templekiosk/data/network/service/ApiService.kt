package com.xenia.templekiosk.data.network.service

import com.xenia.templekiosk.data.network.model.ApiCategoryResponse
import com.xenia.templekiosk.data.network.model.ApiProductResponse
import com.xenia.templekiosk.data.network.model.CompanyResponse
import com.xenia.templekiosk.data.network.model.LoginResponse
import com.xenia.templekiosk.data.network.model.OrderRequest
import com.xenia.templekiosk.data.network.model.OrderResponse
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.network.model.PaymentResponse
import com.xenia.templekiosk.data.network.model.PaymentStatus
import com.xenia.templekiosk.data.network.model.TK_VazhipaduDetails
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Query("username") userName: String,
        @Query("password") password: String
    ): LoginResponse

    @GET("auth/company")
    suspend fun getCompany(@Query("companyId") companyId: Int): CompanyResponse

    @POST("payment/generateToken")
    suspend fun generateToken(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int
    ): PaymentResponse

    @POST("payment/generateQR")
    suspend fun generateQr(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int,
        @Body request: PaymentRequest
    ): PaymentResponse

    @POST("payment/status")
    suspend fun paymentStatus(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int,
        @Body request: PaymentStatus
    ): PaymentResponse

    @POST("orders")
    suspend fun postOrder(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int,
        @Body request: OrderRequest
    ): OrderResponse


    @GET("category")
    suspend fun generateOfferingCat(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int,
        @Query("subTempleId") subTempleId: Int
    ): ApiCategoryResponse


    @GET("offerings")
    suspend fun generateOffering(
        @Query("userId") userId: Int,
        @Query("companyId") companyId: Int,
        @Query("subTempleId") subTempleId: Int,
        @Query("categoryId") categoryId: Int
    ): ApiProductResponse

    @POST("vazhipadi")
    suspend fun postVazhipadi(
        @Body request: TK_VazhipaduDetails
    ): OrderResponse

}