package com.example.hawiirestaurantapp.Screens.Data

import com.google.firebase.database.FirebaseDatabase

fun saveOrder(order: Map<String, Any>, onDone: (String?, String?) -> Unit) {
    val ref = FirebaseDatabase.getInstance().getReference("Orders").push()
    val orderId = ref.key
    if (orderId == null) {
        onDone(null, "Could not create the order")
        return
    }
    ref.setValue(order).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onDone(orderId, null)
        } else {
            onDone(null, task.exception?.localizedMessage ?: "Could not save your order")
        }
    }
}

fun updateOrder(orderId: String, updates: Map<String, Any>) {
    FirebaseDatabase.getInstance().getReference("Orders").child(orderId).updateChildren(updates)
}