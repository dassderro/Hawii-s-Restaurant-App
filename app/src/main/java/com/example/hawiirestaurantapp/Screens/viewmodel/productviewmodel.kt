package com.example.hawiirestaurantapp.Screens.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.hawiirestaurantapp.Data.deleteProduct
import com.example.hawiirestaurantapp.Data.listenProducts
import com.example.hawiirestaurantapp.Data.saveProduct
import com.example.hawiirestaurantapp.Data.seedProducts
import com.example.hawiirestaurantapp.Data.uploadImage
import com.example.hawiirestaurantapp.Screens.Home.Food

class ProductViewModel : ViewModel() {

    var products by mutableStateOf<List<Food>>(emptyList())
        private set
    var loaded by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var uploading by mutableStateOf(false)
        private set
    var message by mutableStateOf("")

    private val listener = listenProducts(onError = { message = it }) {
        products = it
        loaded = true
    }

    fun save(food: Food, onDone: () -> Unit) {
        saving = true
        message = ""
        saveProduct(food) { err ->
            saving = false
            if (err == null) onDone() else message = err
        }
    }

    fun delete(id: String) {
        deleteProduct(id) { err -> message = err ?: "Product deleted" }
    }

    fun seed() {
        seedProducts { err -> message = err ?: "Sample menu uploaded" }
    }

    fun uploadPhoto(context: Context, uri: Uri, onUploaded: (String) -> Unit) {
        uploading = true
        message = ""
        uploadImage(
            context,
            uri,
            { url ->
                uploading = false
                onUploaded(url)
            },
            { msg ->
                uploading = false
                message = "Photo upload failed: $msg"
            }
        )
    }

    override fun onCleared() {
        listener.remove()
    }
}