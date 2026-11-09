package com.example.hawiirestaurantapp.Screens.splashscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

val HawiiPrimary = Color(0xFFE8281A)
val HawiiSecondary = Color(0xFFF6A623)
val HawiiDark = Color(0xFF1A1A1A)
val HawiiCream = Color(0xFFFFF8F0)

@Composable
fun splashscreen(navController: NavHostController) {
    var started by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (started) 1f else 0f, tween(1000), label = "alpha")
    val scale by animateFloatAsState(if (started) 1f else 0.8f, tween(1000), label = "scale")

    LaunchedEffect(Unit) {
        started = true
        delay(2500.milliseconds)
        navController.navigate("login")
    }

    Box(
        modifier = Modifier.fillMaxSize().background(HawiiCream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).scale(scale)
        ) {
            Box(
                modifier = Modifier.size(110.dp).background(
                    Brush.linearGradient(listOf(HawiiPrimary, HawiiSecondary)), CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("🍴", fontSize = 52.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = HawiiDark)) { append("Haw") }
                    withStyle(SpanStyle(color = HawiiPrimary)) { append("ii") }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "FAST FOOD & RESTAURANT",
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = Color(0xFFAAAAAA)
            )
        }
        CircularProgressIndicator(
            color = HawiiPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp).size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun splashscreenpreview() {
    splashscreen(rememberNavController())
}