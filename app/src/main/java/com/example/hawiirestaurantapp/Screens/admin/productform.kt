package com.example.hawiirestaurantapp.Screens.Admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Data.getProduct
import com.example.hawiirestaurantapp.Screens.Home.CategoryChip
import com.example.hawiirestaurantapp.Screens.Home.Food
import com.example.hawiirestaurantapp.Screens.Home.FoodImage
import com.example.hawiirestaurantapp.Screens.Home.categories
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiSecondary
import com.example.hawiirestaurantapp.Screens.viewmodel.ProductViewModel

@Composable
fun FormField(label: String, value: String, onChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HawiiPrimary,
            focusedLabelColor = HawiiPrimary,
            cursorColor = HawiiPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun ProductFormScreen(navController: NavHostController, productId: String) {
    val context = LocalContext.current
    val viewModel: ProductViewModel = viewModel()
    val isNew = productId == "new"
    var loaded by remember { mutableStateOf(isNew) }
    var original by remember { mutableStateOf(Food()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var oldPrice by remember { mutableStateOf("") }
    var badge by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var chef by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        if (!isNew) {
            getProduct(productId) { food ->
                if (food != null) {
                    original = food
                    name = food.name
                    description = food.description
                    category = food.category
                    price = food.price.toString()
                    oldPrice = if (food.oldPrice > 0) food.oldPrice.toString() else ""
                    badge = food.badge
                    ingredients = food.ingredients
                    calories = if (food.calories > 0) food.calories.toString() else ""
                    chef = food.chef
                    imageUrl = food.imageUrl
                } else {
                    error = "Could not load this product"
                }
                loaded = true
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            error = ""
            viewModel.uploadPhoto(context, uri) { url -> imageUrl = url }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(HawiiCream).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text(
                if (isNew) "Add Product" else "Edit Product",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = HawiiDark
            )
        }
        if (!loaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HawiiPrimary)
            }
        } else {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    FoodImage(original.copy(imageUrl = imageUrl), Modifier.fillMaxSize())
                    if (viewModel.uploading) {
                        CircularProgressIndicator(color = HawiiPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !viewModel.uploading,
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (viewModel.uploading) "Uploading photo..." else "Choose Photo")
                }
                Spacer(modifier = Modifier.height(16.dp))

                FormField("Name", name, { name = it })
                FormField("Description", description, { description = it }, singleLine = false)
                Text("Category", fontSize = 14.sp, color = HawiiDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((catName, catEmoji) in categories.drop(1)) {
                        CategoryChip(catName, catEmoji, category == catName) { category = catName }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FormField("Price (KES)", price, { price = it }, KeyboardType.Number)
                FormField("Old price (KES, optional)", oldPrice, { oldPrice = it }, KeyboardType.Number)
                FormField("Badge (e.g. Hot, New, Best Seller)", badge, { badge = it })
                FormField("Ingredients (separate with commas)", ingredients, { ingredients = it }, singleLine = false)
                FormField("Calories", calories, { calories = it }, KeyboardType.Number)
                FormField("Chef", chef, { chef = it })

                val shownError = error.ifEmpty { viewModel.message }
                if (shownError.isNotEmpty()) {
                    Text(shownError, color = HawiiPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        val priceInt = price.toIntOrNull()
                        if (name.isBlank() || category.isBlank()) {
                            error = "Name and category are required"
                        } else if (priceInt == null || priceInt <= 0) {
                            error = "Enter a valid price"
                        } else {
                            error = ""
                            val food = original.copy(
                                name = name.trim(),
                                description = description.trim(),
                                category = category,
                                price = priceInt,
                                oldPrice = oldPrice.toIntOrNull() ?: 0,
                                badge = badge.trim(),
                                imageUrl = imageUrl,
                                ingredients = ingredients.trim(),
                                calories = calories.toIntOrNull() ?: 0,
                                chef = chef.trim(),
                                createdAt = if (original.createdAt == 0L) System.currentTimeMillis() else original.createdAt
                            )
                            viewModel.save(food) { navController.popBackStack() }
                        }
                    },
                    enabled = !viewModel.uploading && !viewModel.saving,
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (viewModel.saving) "Saving..." else if (isNew) "Add Product" else "Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}