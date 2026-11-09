package com.example.hawiirestaurantapp.Screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary

@Composable
fun UserOrdersScreen(navController: NavHostController) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = listenUserOrders(
            onError = { errorMessage = it },
            onResult = { userOrders -> orders = userOrders }
        )
        onDispose { listener.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HawiiCream)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text("My Orders & Tracking", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage.isNotEmpty()) {
            Text("Could not load orders: " + errorMessage, fontSize = 13.sp, color = HawiiPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (orders.isEmpty() && errorMessage.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have not placed any orders yet.", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(orders) { order ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status", fontSize = 12.sp, color = Color.Gray)
                                if (order.estimatedTimeMinutes > 0 && order.status != STATUS_COMPLETED && order.status != STATUS_DELIVERED) {
                                    Text("Est. ~${order.estimatedTimeMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                                } else {
                                    Text(order.status, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(order.status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HawiiDark)

                            Spacer(modifier = Modifier.height(12.dp))
                            OrderProgressStepper(order.status)

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Payment: " + order.paymentStatus + " (" + order.paymentMethod.ifBlank { "M-Pesa" } + ")", fontSize = 12.sp, color = Color.Gray)

                            if (order.deliveryAddress.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Delivery Address: ${order.deliveryAddress}", fontSize = 12.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Items:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HawiiDark)
                            for (item in order.items) {
                                Text("• ${item.quantity}x ${item.name} (KES ${item.price * item.quantity})", fontSize = 12.sp, color = Color.DarkGray)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Paid:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HawiiDark)
                                Text("KES ${order.totalPrice}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}