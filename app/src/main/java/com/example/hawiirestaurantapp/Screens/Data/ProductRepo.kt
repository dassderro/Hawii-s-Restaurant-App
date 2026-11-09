package com.example.hawiirestaurantapp.Data

import com.example.hawiirestaurantapp.Screens.Home.Food
import com.example.hawiirestaurantapp.Screens.Home.sampleFoods
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private fun productsRef() = FirebaseDatabase.getInstance().getReference("Products")

private fun foodToMap(food: Food): HashMap<String, Any> = hashMapOf(
    "name" to food.name,
    "description" to food.description,
    "category" to food.category,
    "price" to food.price,
    "oldPrice" to food.oldPrice,
    "badge" to food.badge,
    "reviews" to food.reviews,
    "imageName" to food.imageName,
    "imageUrl" to food.imageUrl,
    "ingredients" to food.ingredients,
    "calories" to food.calories,
    "chef" to food.chef,
    "createdAt" to food.createdAt
)

class ProductListener(private val listener: ValueEventListener) {
    fun remove() {
        productsRef().removeEventListener(listener)
    }
}

fun listenProducts(onError: (String) -> Unit = {}, onChange: (List<Food>) -> Unit): ProductListener {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onChange(
                snapshot.children
                    .mapNotNull { child -> child.getValue(Food::class.java)?.copy(id = child.key ?: "") }
                    .sortedBy { it.createdAt }
            )
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.message)
        }
    }
    productsRef().addValueEventListener(listener)
    return ProductListener(listener)
}

fun getProduct(id: String, onResult: (Food?) -> Unit) {
    productsRef().child(id).get().addOnCompleteListener { task ->
        onResult(if (task.isSuccessful) task.result?.getValue(Food::class.java)?.copy(id = id) else null)
    }
}

fun saveProduct(food: Food, onDone: (String?) -> Unit) {
    val ref = if (food.id.isBlank()) productsRef().push() else productsRef().child(food.id)
    ref.setValue(foodToMap(food)).addOnCompleteListener { task ->
        onDone(if (task.isSuccessful) null else task.exception?.localizedMessage ?: "Save failed")
    }
}

fun deleteProduct(id: String, onDone: (String?) -> Unit) {
    productsRef().child(id).removeValue().addOnCompleteListener { task ->
        onDone(if (task.isSuccessful) null else task.exception?.localizedMessage ?: "Delete failed")
    }
}

fun seedProducts(onDone: (String?) -> Unit) {
    val updates = HashMap<String, Any>()
    sampleFoods.forEachIndexed { index, food ->
        updates["seed_" + food.id] = foodToMap(food.copy(createdAt = index.toLong()))
    }
    productsRef().updateChildren(updates).addOnCompleteListener { task ->
        onDone(if (task.isSuccessful) null else task.exception?.localizedMessage ?: "Upload failed")
    }
}