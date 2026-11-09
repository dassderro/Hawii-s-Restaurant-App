package com.example.hawiirestaurantapp.Screens.orders

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class OrderItem(
    val foodId: String = "",
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val imageUrl: String = ""
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val deliveryAddress: String = "",
    val mapsLink: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Int = 0,
    val status: String = STATUS_PAYMENT_PENDING,
    val paymentStatus: String = "Pending",
    val paymentMethod: String = "M-Pesa",
    val estimatedTimeMinutes: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)

const val STATUS_PAYMENT_PENDING = "Payment Pending"
const val STATUS_PAYMENT_CONFIRMED = "Payment Confirmed"
const val STATUS_PAYMENT_FAILED = "Payment Failed"
const val STATUS_PREPARING = "Preparing"
const val STATUS_RIDER_COMING = "Rider is coming to pick your order"
const val STATUS_RIDER_PICKED = "Rider has picked your order"
const val STATUS_DELIVERED = "Delivered"
const val STATUS_COMPLETED = "Completed"

val ORDER_STATUSES = listOf(
    STATUS_PAYMENT_PENDING,
    STATUS_PREPARING,
    STATUS_RIDER_COMING,
    STATUS_RIDER_PICKED,
    STATUS_DELIVERED,
    STATUS_COMPLETED
)

fun calculateEstimatedTime(status: String, address: String): Int {
    val locationBonus = when {
        address.contains("CBD", ignoreCase = true) || address.contains("Town", ignoreCase = true) -> 5
        address.length > 25 -> 15
        else -> 10
    }
    return when (status) {
        STATUS_PREPARING -> 25 + locationBonus
        STATUS_RIDER_COMING -> 15 + locationBonus
        STATUS_RIDER_PICKED -> 10 + (locationBonus / 2)
        STATUS_DELIVERED -> 0
        STATUS_COMPLETED -> 0
        else -> 30
    }
}

private fun ordersRef() = FirebaseDatabase.getInstance().getReference("Orders")

private fun snapshotToOrder(snapshot: DataSnapshot): Order {
    val items = snapshot.child("items").children.mapNotNull { child ->
        val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
        OrderItem(
            foodId = child.child("foodId").getValue(String::class.java) ?: "",
            name = name,
            price = child.child("price").getValue(Long::class.java)?.toInt() ?: 0,
            quantity = child.child("quantity").getValue(Long::class.java)?.toInt() ?: 1,
            imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
        )
    }
    return Order(
        id = snapshot.key ?: "",
        userId = snapshot.child("userId").getValue(String::class.java) ?: "",
        userName = snapshot.child("orderName").getValue(String::class.java) ?: "Customer",
        userPhone = snapshot.child("phone").getValue(String::class.java) ?: "",
        deliveryAddress = snapshot.child("address").getValue(String::class.java) ?: "",
        mapsLink = snapshot.child("mapsLink").getValue(String::class.java) ?: "",
        items = items,
        totalPrice = snapshot.child("total").getValue(Long::class.java)?.toInt() ?: 0,
        status = snapshot.child("orderStatus").getValue(String::class.java) ?: STATUS_PAYMENT_PENDING,
        paymentStatus = snapshot.child("paymentStatus").getValue(String::class.java) ?: "Pending",
        paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java) ?: "M-Pesa",
        estimatedTimeMinutes = snapshot.child("estimatedTimeMinutes").getValue(Long::class.java)?.toInt() ?: 30,
        createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
    )
}

class OrdersListener(private val listener: ValueEventListener) {
    fun remove() {
        ordersRef().removeEventListener(listener)
    }
}

fun listenUserOrders(onError: (String) -> Unit = {}, onResult: (List<Order>) -> Unit): OrdersListener {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val list = snapshot.children
                .map { snapshotToOrder(it) }
                .filter { it.userId == uid }
                .sortedByDescending { it.createdAt }
            onResult(list)
        }
        override fun onCancelled(error: DatabaseError) {
            onError(error.message)
            onResult(emptyList())
        }
    }
    ordersRef().addValueEventListener(listener)
    return OrdersListener(listener)
}

fun listenAllOrders(onError: (String) -> Unit = {}, onResult: (List<Order>) -> Unit): OrdersListener {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onResult(snapshot.children.map { snapshotToOrder(it) }.sortedByDescending { it.createdAt })
        }
        override fun onCancelled(error: DatabaseError) {
            onError(error.message)
            onResult(emptyList())
        }
    }
    ordersRef().addValueEventListener(listener)
    return OrdersListener(listener)
}

fun updateOrderStatus(orderId: String, newStatus: String, newEstimate: Int, onComplete: (Boolean) -> Unit) {
    ordersRef().child(orderId).updateChildren(
        mapOf(
            "orderStatus" to newStatus,
            "estimatedTimeMinutes" to newEstimate
        )
    ).addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}