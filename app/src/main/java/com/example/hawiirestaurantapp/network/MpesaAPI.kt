package com.example.hawiirestaurantapp.network

import com.example.hawiirestaurantapp.models.MpesaTokenResponse
import com.example.hawiirestaurantapp.models.StkPushRequest
import com.example.hawiirestaurantapp.models.StkPushResponse
import com.example.hawiirestaurantapp.models.StkQueryRequest
import com.example.hawiirestaurantapp.models.StkQueryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MpesaAPI {
    @GET("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getToken(@Header("Authorization") authorization: String): Response<MpesaTokenResponse>

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun stkPush(
        @Header("Authorization") authorization: String,
        @Body request: StkPushRequest
    ): Response<StkPushResponse>

    @POST("mpesa/stkpushquery/v1/query")
    suspend fun stkQuery(
        @Header("Authorization") authorization: String,
        @Body request: StkQueryRequest
    ): Response<StkQueryResponse>
}