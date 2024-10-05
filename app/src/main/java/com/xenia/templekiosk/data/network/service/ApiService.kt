package com.xenia.templekiosk.data.network.service

import com.xenia.templekiosk.data.network.model.LoginResponse
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.network.model.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Path("username") userName: String,
                      @Path("password") password: String): LoginResponse

    @POST("payment/cafe/generateToken")
    suspend fun generateToken(@Query("MiD") mid: String): PaymentResponse

    @POST("payment/cafe/generateQR")
    suspend fun generateQr(@Query("MiD") mid: String,@Body request: PaymentRequest): PaymentResponse

   /* @GET("auth/user")
    suspend fun getUserDetails(@HeaderMap headers: Map<String, String>): UserResponse

    @PUT("token/IsAnnounced/{companyId}/{departmentId}/{tokenValue}")
    suspend fun updateToken(
        @Path("companyId") companyId: String,
        @Path("departmentId") departmentId: String,
        @Path("tokenValue") tokenValue: String
    ): Response<Unit>

    @GET("/api/token/onTokenAudio/{tokenNumber}/{counterName}")
    suspend fun getTokenAudio(
        @Path("tokenNumber") tokenNumber: String,
        @Path("counterName") counterName: String
    ): ResponseBody

    @GET("/api/advertisement/{companyId}/{depId}")
    suspend fun getAdvertisement(
        @Path("companyId") companyId: String,
        @Path("depId") depId: String
    ): AdvertisementResponse*/
}