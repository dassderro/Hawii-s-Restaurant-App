package com.example.hawiirestaurantapp.Screens.Home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.hawiirestaurantapp.Screens.Data.CartManager
import com.example.hawiirestaurantapp.Data.listenProducts
import com.example.hawiirestaurantapp.Navigation.ROUTE_CART
import androidx.activity.compose.BackHandler
import com.example.hawiirestaurantapp.Navigation.ROUTE_HOME
import com.example.hawiirestaurantapp.Navigation.ROUTE_PROFILE
import com.example.hawiirestaurantapp.Navigation.ROUTE_USER_ORDERS
import com.example.hawiirestaurantapp.Navigation.ROUTE_LOGIN
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiSecondary
import com.google.firebase.auth.FirebaseAuth

data class Food(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Int = 0,
    val oldPrice: Int = 0,
    val badge: String = "",
    val reviews: Int = 0,
    val imageName: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val calories: Int = 0,
    val chef: String = "",
    val createdAt: Long = 0
)

val sampleFoods = listOf(
    Food("1", "Classic Smash Burger", "Double smashed patty, cheddar, caramelized onions, pickles & special sauce", "Burgers", 1950, 2450, "Hot", 128, "smash_burger"),
    Food("2", "Margherita Royale", "San Marzano tomatoes, buffalo mozzarella, basil & truffle oil on sourdough", "Pizza", 2600, 3200, "New", 95, "margherita_pizza"),
    Food("3", "Nashville Hot Chicken", "Crispy fried chicken in fiery Nashville spice blend with honey drizzle", "Chicken", 1700, 2200, "Best Seller", 210, "nashville_chicken"),
    Food("4", "Loaded Fajita Wrap", "Grilled chicken, peppers, sour cream & guacamole in a warm tortilla", "Wraps", 1400, 0, "", 74, "fajita_wrap"),
    Food("5", "Ice Cream Sundae", "Molten chocolate cake with Nutella center, vanilla ice cream & caramel", "Desserts", 1150, 1550, "New", 56, "ice_cream_sundae"),
    Food("6", "Truffle Mushroom Pasta", "Al dente tagliatelle, wild mushrooms, black truffle, parmesan & thyme", "Pasta", 2200, 0, "Chef's Pick", 88, "truffle_pasta")
)

val categories = listOf(
    "All" to "🍽️",
    "Burgers" to "🍔",
    "Pizza" to "🍕",
    "Chicken" to "🍗",
    "Wraps" to "🌯",
    "Pasta" to "🍝",
    "Desserts" to "🍰",
    "Fries" to "🍟",
    "Drinks" to "🥤"
)

@Composable
fun HomeScreen(navController: NavHostController) {
    BackHandler(enabled = true) { }
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }
    var products by remember { mutableStateOf<List<Food>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = listenProducts { products = it }
        onDispose { listener.remove() }
    }

    val menu = products.ifEmpty { sampleFoods }
    val foods = menu.filter {
        (selected == "All" || it.category == selected) && it.name.contains(search, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HawiiCream)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "What are you craving today?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = HawiiDark
                )
            }
            IconButton(onClick = { navController.navigate(ROUTE_CART) }) {
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
            IconButton(onClick = { navController.navigate(ROUTE_PROFILE) }) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = HawiiDark)
            }
            IconButton(onClick = {
                CartManager.clear()
                FirebaseAuth.getInstance().signOut()
                navController.navigate(ROUTE_LOGIN) {
                    popUpTo(ROUTE_HOME) { inclusive = true }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = HawiiPrimary)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search for food...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HawiiPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                cursorColor = HawiiPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories) { (name, emoji) ->
                CategoryChip(name, emoji, selected == name) { selected = name }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Popular Foods", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        Spacer(modifier = Modifier.height(8.dp))
        if (foods.isEmpty()) {
            Text("No food found", fontSize = 14.sp, color = Color.Gray)
        }

        // Scrollable food list taking up weighted space
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(foods) { food ->
                FoodCard(
                    food = food,
                    onClick = { navController.navigate("food/" + food.id) },
                    onAdd = {
                        CartManager.add(food)
                        Toast.makeText(context, food.name + " added to cart", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ONLY the Orders button fixed at the bottom of the page content
        Button(
            onClick = { navController.navigate(ROUTE_USER_ORDERS) },
            colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("View My Orders & Tracking", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CategoryChip(name: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) HawiiPrimary else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (selected) Color.White else HawiiDark)
    }
}

@Composable
fun FoodCard(food: Food, onClick: () -> Unit, onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodImage(food, Modifier.size(88.dp).clip(RoundedCornerShape(14.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (food.badge.isNotEmpty()) {
                    Text(
                        food.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(HawiiSecondary).padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(food.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                Text(food.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("KES " + "%,d".format(food.price), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                    if (food.oldPrice > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("%,d".format(food.oldPrice), fontSize = 12.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                    }
                }
                Text("⭐ (" + food.reviews + " reviews)", fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier.size(36.dp).background(HawiiPrimary, CircleShape).clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}