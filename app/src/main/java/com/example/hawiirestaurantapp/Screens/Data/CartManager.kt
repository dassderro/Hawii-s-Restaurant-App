package com.example.hawiirestaurantapp.Screens.Data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hawiirestaurantapp.Screens.Home.Food

data class CartItem(val food: Food, val quantity: Int)

object CartManager {
    val items = mutableStateListOf<CartItem>()
    var notes by mutableStateOf("")

    val totalCount: Int get() = items.sumOf { it.quantity }
    val totalPrice: Int get() = items.sumOf { it.food.price * it.quantity }

    fun add(food: Food) {
        val index = items.indexOfFirst { it.food.id == food.id }
        if (index >= 0) {
            items[index] = items[index].copy(quantity = items[index].quantity + 1)
        } else {
            items.add(CartItem(food, 1))
        }
    }

    fun decrease(food: Food) {
        val index = items.indexOfFirst { it.food.id == food.id }
        if (index >= 0) {
            val item = items[index]
            if (item.quantity > 1) {
                items[index] = item.copy(quantity = item.quantity - 1)
            } else {
                items.removeAt(index)
            }
        }
    }

    fun remove(food: Food) {
        items.removeAll { it.food.id == food.id }
    }

    fun clear() {
        items.clear()
        notes = ""
    }
}