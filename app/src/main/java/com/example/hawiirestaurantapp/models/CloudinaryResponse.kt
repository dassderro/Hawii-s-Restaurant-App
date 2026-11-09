package com.example.hawii.screens.models

import com.google.gson.annotations.SerializedName

@SerializedName("secure_url") val secureUrl: String? = null
data class CloudinaryResponse(
    val url: String? = null,
    val secure_url: String? = null,
    val public_id: String? = null
)

