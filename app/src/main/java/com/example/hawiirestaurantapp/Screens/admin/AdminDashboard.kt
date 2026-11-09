package com.example.hawiirestaurantapp.Screens.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Navigation.ROUTE_ADMIN
import com.example.hawiirestaurantapp.Navigation.ROUTE_ADMIN_ORDERS
import com.example.hawiirestaurantapp.Navigation.ROUTE_LOGIN
import com.example.hawiirestaurantapp.Navigation.ROUTE_PROFILE
import androidx.activity.compose.BackHandler
import com.example.hawiirestaurantapp.Screens.Home.Food
import com.example.hawiirestaurantapp.Screens.Home.FoodImage
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    BackHandler(enabled = true) { }
    val viewModel: ProductViewModel = viewModel()
    val products = viewModel.products
    var deleteTarget by remember { mutableStateOf<Food?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HawiiCream)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Admin Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = HawiiDark
                )
                Text("Manage menu & live customer orders", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(ROUTE_LOGIN) {
                    popUpTo(ROUTE_ADMIN) { inclusive = true }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = HawiiPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { navController.navigate(ROUTE_ADMIN_ORDERS) },
                colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Orders", fontSize = 14.sp)
            }
            Button(
                onClick = { navController.navigate("productform/new") },
                colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("+ Add Food", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Menu Products (" + products.size + ")",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = HawiiDark
            )
        }

        if (viewModel.message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(viewModel.message, fontSize = 13.sp, color = HawiiPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (!viewModel.loaded) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HawiiPrimary)
            }
        } else if (products.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No products added yet.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.seed() },
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload Sample Menu")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { food ->
                    AdminFoodCard(
                        food = food,
                        onEdit = { navController.navigate("productform/" + food.id) },
                        onDelete = { deleteTarget = food }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate(ROUTE_PROFILE) },
            colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("My Profile", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete " + target.name + "?") },
            text = { Text("This action will permanently remove this item from the store menu.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
                    deleteTarget = null
                }) {
                    Text("Delete", color = HawiiPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = HawiiDark)
                }
            }
        )
    }
}

@Composable
fun AdminFoodCard(food: Food, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodImage(food, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
                Text(food.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(2.dp))
                Text("KES " + "%,d".format(food.price), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = HawiiDark)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HawiiPrimary)
            }
        }
    }
}