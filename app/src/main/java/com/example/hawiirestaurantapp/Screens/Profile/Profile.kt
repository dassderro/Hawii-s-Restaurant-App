package com.example.hawiirestaurantapp.Screens.Profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.hawiirestaurantapp.Screens.orders.Order
import com.example.hawiirestaurantapp.Screens.orders.STATUS_COMPLETED
import com.example.hawiirestaurantapp.Screens.orders.listenUserOrders
import com.example.hawiirestaurantapp.Screens.Admin.FormField
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiSecondary
import com.example.hawiirestaurantapp.Screens.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel()
    val profile = viewModel.profile

    var editing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    var userOrders by remember { mutableStateOf<List<Order>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    DisposableEffect(Unit) {
        val listener = listenUserOrders { orders: List<Order> ->
            userOrders = orders
        }
        onDispose { listener.remove() }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.uploadProfilePhoto(context, uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(HawiiCream).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text("My Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        }

        if (!viewModel.profileLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HawiiPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(HawiiPrimary, HawiiSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            profile.fullname.trim().take(1).uppercase().ifEmpty { "?" },
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    if (viewModel.uploading) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                TextButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !viewModel.uploading
                ) {
                    Text(if (viewModel.uploading) "Uploading..." else "Change Photo", color = HawiiPrimary)
                }
                Text(
                    profile.fullname.ifBlank { "Your name" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = HawiiDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (editing) {
                    FormField("Full Name", editName, { editName = it })
                    FormField("Phone Number", editPhone, { editPhone = it }, KeyboardType.Phone)
                    FormField("Delivery Address", editAddress, { editAddress = it }, singleLine = false)
                    Text("Email: " + profile.email + " (can't be changed here)", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { editing = false },
                            colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, editPhone, editAddress) { saved ->
                                    if (saved) editing = false
                                }
                            },
                            enabled = !viewModel.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text(if (viewModel.loading) "Saving..." else "Save")
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            ProfileRow("Full Name", profile.fullname)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HawiiCream))
                            ProfileRow("Phone Number", profile.phone)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HawiiCream))
                            ProfileRow("Email", profile.email)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HawiiCream))
                            ProfileRow("Address", profile.address)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            editName = profile.fullname
                            editPhone = profile.phone
                            editAddress = profile.address
                            viewModel.message = ""
                            editing = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (viewModel.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(viewModel.message, fontSize = 13.sp, color = HawiiPrimary)
                }

                // Current Active Orders Section
                val activeOrders: List<Order> = userOrders.filter { order -> order.status != STATUS_COMPLETED }
                if (activeOrders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Current Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = HawiiDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    for (order in activeOrders) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status", fontSize = 12.sp, color = Color.Gray)
                                    if (order.estimatedTimeMinutes > 0) {
                                        Text("Est. ~${order.estimatedTimeMinutes} min", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                                    } else {
                                        Text("Arrived", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(order.status, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HawiiDark)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Ordered Items:", fontSize = 12.sp, color = Color.Gray)
                                for (item in order.items) {
                                    Text("• ${item.quantity}x ${item.name}", fontSize = 13.sp, color = HawiiDark)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value.ifBlank { "Not added yet" },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (value.isBlank()) Color.Gray else HawiiDark
        )
    }
}