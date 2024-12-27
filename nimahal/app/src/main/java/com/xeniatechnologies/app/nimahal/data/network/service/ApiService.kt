package com.xeniatechnologies.app.nimahal.data.network.service

import com.xeniatechnologies.app.nimahal.data.network.model.PaymentRequest
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentResponse
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentStatus
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

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



}