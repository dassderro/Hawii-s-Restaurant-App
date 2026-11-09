package com.example.hawiirestaurantapp.models

import com.google.gson.annotations.SerializedName

data class MpesaTokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("expires_in") val expiresIn: String? = null
)

data class StkPushRequest(
    @SerializedName("BusinessShortCode") val businessShortCode: String,
    @SerializedName("Password") val password: String,
    @SerializedName("Timestamp") val timestamp: String,
    @SerializedName("TransactionType") val transactionType: String,
    @SerializedName("Amount") val amount: String,
    @SerializedName("PartyA") val partyA: String,
    @SerializedName("PartyB") val partyB: String,
    @SerializedName("PhoneNumber") val phoneNumber: String,
    @SerializedName("CallBackURL") val callBackUrl: String,
    @SerializedName("AccountReference") val accountReference: String,
    @SerializedName("TransactionDesc") val transactionDesc: String
)

data class StkPushResponse(
    @SerializedName("MerchantRequestID") val merchantRequestId: String? = null,
    @SerializedName("CheckoutRequestID") val checkoutRequestId: String? = null,
    @SerializedName("ResponseCode") val responseCode: String? = null,
    @SerializedName("ResponseDescription") val responseDescription: String? = null,
    @SerializedName("CustomerMessage") val customerMessage: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
    @SerializedName("errorMessage") val errorMessage: String? = null
)

data class StkQueryRequest(
    @SerializedName("BusinessShortCode") val businessShortCode: String,
    @SerializedName("Password") val password: String,
    @SerializedName("Timestamp") val timestamp: String,
    @SerializedName("CheckoutRequestID") val checkoutRequestId: String
)

data class StkQueryResponse(
    @SerializedName("ResponseCode") val responseCode: String? = null,
    @SerializedName("ResponseDescription") val responseDescription: String? = null,
    @SerializedName("CheckoutRequestID") val checkoutRequestId: String? = null,
    @SerializedName("ResultCode") val resultCode: String? = null,
    @SerializedName("ResultDesc") val resultDesc: String? = null
)