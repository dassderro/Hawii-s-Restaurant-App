package com.example.hawiirestaurantapp.Screens.Cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Screens.Data.CartItem
import com.example.hawiirestaurantapp.Screens.Data.CartManager
import com.example.hawiirestaurantapp.Screens.Home.FoodImage
import com.example.hawiirestaurantapp.Navigation.ROUTE_CHECKOUT
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary

@Composable
fun CartScreen(navController: NavHostController) {
    val cartItems = CartManager.items

    Column(modifier = Modifier.fillMaxSize().background(HawiiCream).statusBarsPadding().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text("My Cart", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        }

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🛒", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your cart is empty", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                Text("Add something tasty from the menu", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Browse Menu")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.food.id }) { item ->
                    CartItemCard(item)
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Order Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = CartManager.notes,
                        onValueChange = { CartManager.notes = it },
                        placeholder = { Text("Allergies or special requests...") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HawiiPrimary,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            cursorColor = HawiiPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("KES " + "%,d".format(CartManager.totalPrice), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate(ROUTE_CHECKOUT) },
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Checkout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodImage(item.food, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.food.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                Text("KES " + "%,d".format(item.food.price) + " each", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QtyButton("-") { CartManager.decrease(item.food) }
                    Text(
                        item.quantity.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HawiiDark,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    QtyButton("+") { CartManager.add(item.food) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { CartManager.remove(item.food) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = HawiiPrimary)
                }
                Text("KES " + "%,d".format(item.food.price * item.quantity), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
            }
        }
    }
}

@Composable
fun QtyButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(30.dp).background(HawiiCream, CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HawiiDark)
    }
}