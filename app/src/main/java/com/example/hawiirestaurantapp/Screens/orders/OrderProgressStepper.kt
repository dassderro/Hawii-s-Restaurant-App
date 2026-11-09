package com.example.hawiirestaurantapp.Screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary

private data class StepDef(val status: String, val label: String)

private val STEP_DEFS = listOf(
    StepDef(STATUS_PAYMENT_PENDING, "Order Placed"),
    StepDef(STATUS_PREPARING, "Preparing"),
    StepDef(STATUS_RIDER_COMING, "Picking Up"),
    StepDef(STATUS_RIDER_PICKED, "On The Way"),
    StepDef(STATUS_DELIVERED, "Delivered")
)

@Composable
fun OrderProgressStepper(currentStatus: String) {
    val currentIndex = when (currentStatus) {
        STATUS_PAYMENT_FAILED -> -1
        STATUS_COMPLETED -> STEP_DEFS.lastIndex
        else -> STEP_DEFS.indexOfFirst { it.status == currentStatus }.let { if (it == -1) 0 else it }
    }

    if (currentStatus == STATUS_PAYMENT_FAILED) {
        Text("Payment was not completed", fontSize = 12.sp, color = HawiiPrimary, fontWeight = FontWeight.Bold)
        return
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        STEP_DEFS.forEachIndexed { index, step ->
            val done = index <= currentIndex
            val circleColor = if (done) HawiiPrimary else Color(0xFFE0E0E0)

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    step.label,
                    fontSize = 9.sp,
                    color = if (done) HawiiDark else Color.Gray,
                    maxLines = 2
                )
            }

            if (index != STEP_DEFS.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentIndex) HawiiPrimary else Color(0xFFE0E0E0))
                )
            }
        }
    }
}