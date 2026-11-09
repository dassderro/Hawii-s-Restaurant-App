package com.example.hawiirestaurantapp.Screens.Admin

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.widget.Toast
import com.example.hawiirestaurantapp.Screens.orders.Order
import com.example.hawiirestaurantapp.Screens.orders.ORDER_STATUSES
import com.example.hawiirestaurantapp.Screens.orders.calculateEstimatedTime
import com.example.hawiirestaurantapp.Screens.orders.listenAllOrders
import com.example.hawiirestaurantapp.Screens.orders.updateOrderStatus
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary

@Composable
fun AdminOrdersScreen(navController: NavHostController) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = listenAllOrders(
            onError = { },
            onResult = { result ->
                orders = result
                loaded = true
            }
        )
        onDispose { listener.remove() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(HawiiCream).statusBarsPadding().padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text(
                "Placed Orders (" + orders.size + ")",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = HawiiDark
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!loaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HawiiPrimary)
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No customer orders placed yet.", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    AdminOrderCard(order = order)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderCard(order: Order) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Customer: " + order.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HawiiDark)
                Text("KES " + order.totalPrice, fontWeight = FontWeight.Bold, color = HawiiPrimary)
            }
            if (order.userPhone.isNotBlank()) {
                Text("Phone: " + order.userPhone, fontSize = 13.sp, color = Color.Gray)
            }
            if (order.deliveryAddress.isNotBlank()) {
                Text("Address: " + order.deliveryAddress, fontSize = 13.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Items:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HawiiDark)
            for (item in order.items) {
                Text("• " + item.quantity + "x " + item.name + " (KES " + (item.price * item.quantity) + ")", fontSize = 12.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Update Status:", fontSize = 12.sp, color = Color.Gray)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = order.status + " (~" + order.estimatedTimeMinutes + " min)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ORDER_STATUSES.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                expanded = false
                                val newTime = calculateEstimatedTime(status, order.deliveryAddress)
                                updateOrderStatus(order.id, status, newTime) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Status updated to " + status, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}