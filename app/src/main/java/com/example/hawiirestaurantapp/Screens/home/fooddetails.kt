package com.example.hawiirestaurantapp.Screens.Home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Screens.Data.CartManager
import com.example.hawiirestaurantapp.Navigation.ROUTE_CART
import com.example.hawiirestaurantapp.Screens.Cart.QtyButton
import com.example.hawiirestaurantapp.Screens.Home.FoodImage
import com.example.hawiirestaurantapp.Screens.Home.sampleFoods
import com.example.hawiirestaurantapp.Data.getProduct
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiSecondary

@Composable
fun FoodDetailScreen(navController: NavHostController, foodId: String) {
    val context = LocalContext.current
    var food by remember { mutableStateOf(sampleFoods.find { it.id == foodId }) }
    var done by remember { mutableStateOf(false) }
    var qty by remember { mutableStateOf(1) }

    LaunchedEffect(foodId) {
        getProduct(foodId) { found ->
            if (found != null) food = found
            done = true
        }
    }

    val current = food
    if (current == null) {
        Box(modifier = Modifier.fillMaxSize().background(HawiiCream), contentAlignment = Alignment.Center) {
            if (done) {
                Text("Food not found", fontSize = 16.sp, color = Color.Gray)
            } else {
                CircularProgressIndicator(color = HawiiPrimary)
            }
        }
    } else {
        val ingredientList = current.ingredients.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        Column(modifier = Modifier.fillMaxSize().background(HawiiCream)) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                ) {
                    FoodImage(current, Modifier.fillMaxSize())
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RoundIconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
                        }
                        RoundIconButton(onClick = { navController.navigate(ROUTE_CART) }) {
                            BadgedBox(badge = {
                                if (CartManager.totalCount > 0) {
                                    Badge(containerColor = HawiiPrimary, contentColor = Color.White) {
                                        Text(CartManager.totalCount.toString())
                                    }
                                }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = HawiiDark)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    if (current.badge.isNotEmpty()) {
                        Text(
                            current.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(HawiiSecondary).padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(current.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("KES " + "%,d".format(current.price), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                        if (current.oldPrice > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("%,d".format(current.oldPrice), fontSize = 14.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    Text("⭐ (" + current.reviews + " reviews)", fontSize = 13.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(current.description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCard(
                            "Calories",
                            if (current.calories > 0) current.calories.toString() + " kcal" else "Not added yet",
                            Modifier.weight(1f)
                        )
                        InfoCard(
                            "Chef",
                            if (current.chef.isNotBlank()) current.chef else "Not added yet",
                            Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ingredients", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (ingredientList.isEmpty()) {
                        Text("Not added yet", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        ingredientList.forEach { ingredient ->
                            Text("•  " + ingredient, fontSize = 14.sp, color = HawiiDark, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QtyButton("-") { if (qty > 1) qty -= 1 }
                Text(
                    qty.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HawiiDark,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                QtyButton("+") { qty += 1 }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        repeat(qty) { CartManager.add(current) }
                        Toast.makeText(context, qty.toString() + " x " + current.name + " added to cart", Toast.LENGTH_SHORT).show()
                        qty = 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    // Fixed missing '*' multiplication operator below:
                    Text("Add to Cart • KES " + "%,d".format(current.price * qty),
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.9f), CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HawiiDark)
        }
    }
}