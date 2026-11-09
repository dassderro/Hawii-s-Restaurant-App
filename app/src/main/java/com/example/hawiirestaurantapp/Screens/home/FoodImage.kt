package com.example.hawiirestaurantapp.Screens.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage

@Suppress("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun FoodImage(food: Food, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resId = if (food.imageName.isNotBlank()) context.resources.getIdentifier(food.imageName, "drawable", context.packageName) else 0
    if (food.imageUrl.isNotBlank()) {
        AsyncImage(
            model = food.imageUrl,
            contentDescription = food.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = food.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color(0xFFFEF0DC)))
    }
}