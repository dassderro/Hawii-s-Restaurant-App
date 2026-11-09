package com.example.hawiirestaurantapp.Data

import android.content.Context
import android.net.Uri
import com.example.hawii.screens.network.CloudinaryAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val CLOUDINARY_UPLOAD_PRESET = "Restaurant"

private val cloudinaryApi: CloudinaryAPI by lazy {
    Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudinaryAPI::class.java)
}

fun uploadImage(context: Context, uri: Uri, onUploaded: (String) -> Unit, onFailed: (String) -> Unit) {
    if (CLOUDINARY_UPLOAD_PRESET == "YOUR_UPLOAD_PRESET") {
        onFailed("Set your upload preset name in CloudinaryUploader.kt")
        return
    }
    val appContext = context.applicationContext
    CoroutineScope(Dispatchers.IO).launch {
        var url: String? = null
        var failure = ""
        try {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                failure = "Could not read the photo"
            } else {
                val filePart = MultipartBody.Part.createFormData("file", "photo.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                val presetPart = CLOUDINARY_UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = cloudinaryApi.uploadImage(filePart, presetPart)

                url = response.body()?.secure_url

                if (!response.isSuccessful || url == null) {
                    val raw = response.errorBody()?.string()
                    var cloudMessage: String? = null
                    if (raw != null) {
                        try {
                            cloudMessage = JSONObject(raw).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            cloudMessage = null
                        }
                    }
                    failure = cloudMessage ?: "Cloudinary error " + response.code()
                }
            }
        } catch (e: Exception) {
            failure = e.localizedMessage ?: "Upload failed"
        }
        withContext(Dispatchers.Main) {
            val link = url
            if (link != null) onUploaded(link) else onFailed(failure)
        }
    }
}