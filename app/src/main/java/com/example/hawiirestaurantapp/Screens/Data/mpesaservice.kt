package com.example.hawiirestaurantapp.Screens.Data

import android.util.Base64
import com.example.hawiirestaurantapp.models.StkPushRequest
import com.example.hawiirestaurantapp.models.StkPushResponse
import com.example.hawiirestaurantapp.models.StkQueryRequest
import com.example.hawiirestaurantapp.network.MpesaAPI
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val MPESA_CONSUMER_KEY = "vXWd6lKWYeK7MYdadkP8rFGMGubUoHEbsunC1AFQGhk5twE5"
const val MPESA_CONSUMER_SECRET = "yZZBWUcuo0RMSTX3YGLtwYXTni7iZtBsp6ARpvhnQAdnH22meBy6VGNGSO3uopuI"
const val MPESA_SHORTCODE = "174379"
const val MPESA_PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
const val MPESA_CALLBACK_URL = "https://example.com/mpesa/callback"

private val mpesaApi: MpesaAPI by lazy {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    Retrofit.Builder()
        .baseUrl("https://sandbox.safaricom.co.ke/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MpesaAPI::class.java)
}

fun formatPhone(input: String): String? {
    val digits = input.filter { it.isDigit() }
    return when {
        digits.startsWith("254") && digits.length == 12 -> digits
        digits.startsWith("0") && digits.length == 10 -> "254" + digits.substring(1)
        (digits.startsWith("7") || digits.startsWith("1")) && digits.length == 9 -> "254" + digits
        else -> null
    }
}

private fun mpesaTimestamp(): String {
    val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    format.timeZone = TimeZone.getTimeZone("Africa/Nairobi")
    return format.format(Date())
}

private fun mpesaPassword(timestamp: String): String {
    return Base64.encodeToString((MPESA_SHORTCODE + MPESA_PASSKEY + timestamp).toByteArray(), Base64.NO_WRAP)
}

private suspend fun fetchToken(): String? {
    try {
        val credentials = Base64.encodeToString((MPESA_CONSUMER_KEY + ":" + MPESA_CONSUMER_SECRET).toByteArray(), Base64.NO_WRAP)
        val response = mpesaApi.getToken("Basic " + credentials)
        return response.body()?.accessToken
    } catch (e: Exception) {
        return null
    }
}

suspend fun startStkPush(phone: String, amount: Int, reference: String): Pair<String?, String> {
    try {
        val token = fetchToken() ?: return Pair(null, "Could not connect to M-Pesa. Check your consumer key and secret")
        val time = mpesaTimestamp()
        val body = StkPushRequest(
            businessShortCode = MPESA_SHORTCODE,
            password = mpesaPassword(time),
            timestamp = time,
            transactionType = "CustomerPayBillOnline",
            amount = amount.toString(),
            partyA = phone,
            partyB = MPESA_SHORTCODE,
            phoneNumber = phone,
            callBackUrl = MPESA_CALLBACK_URL,
            accountReference = reference,
            transactionDesc = "Hawii order"
        )
        val response = mpesaApi.stkPush("Bearer " + token, body)
        val result = response.body()
        if (response.isSuccessful && result != null && result.responseCode == "0" && result.checkoutRequestId != null) {
            return Pair(result.checkoutRequestId, result.customerMessage ?: "Check your phone")
        }
        val raw = response.errorBody()?.string()
        val errorMessage = if (raw != null) Gson().fromJson(raw, StkPushResponse::class.java)?.errorMessage else null
        return Pair(null, errorMessage ?: result?.responseDescription ?: "The M-Pesa request failed")
    } catch (e: Exception) {
        return Pair(null, e.localizedMessage ?: "Network error. Check your internet connection")
    }
}

suspend fun checkStkStatus(checkoutRequestId: String): String {
    try {
        val token = fetchToken() ?: return "pending"
        val time = mpesaTimestamp()
        val request = StkQueryRequest(MPESA_SHORTCODE, mpesaPassword(time), time, checkoutRequestId)
        val response = mpesaApi.stkQuery("Bearer " + token, request)
        val result = response.body()
        if (response.isSuccessful && result != null && result.resultCode != null) {
            return if (result.resultCode == "0") "success" else "failed:" + (result.resultDesc ?: "Payment was not completed")
        }
        return "pending"
    } catch (e: Exception) {
        return "pending"
    }
}