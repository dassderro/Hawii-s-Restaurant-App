package com.example.hawiirestaurantapp.Screens.Checkout

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.hawiirestaurantapp.Navigation.ROUTE_HOME
import com.example.hawiirestaurantapp.Screens.Data.CartManager
import com.example.hawiirestaurantapp.Screens.Data.checkStkStatus
import com.example.hawiirestaurantapp.Screens.Data.formatPhone
import com.example.hawiirestaurantapp.Screens.Data.startStkPush
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val MpesaGreen = Color(0xFF2E9E4F)
val PaypalBlue = Color(0xFF0070BA)

fun openInGoogleMaps(context: Context, address: String, lat: Double?, lng: Double?) {
    val query = if (lat != null && lng != null) lat.toString() + "," + lng.toString() else Uri.encode(address)
    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + query))
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + query)))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CheckoutScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cartItems = CartManager.items
    val total = CartManager.totalPrice

    var orderName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationMessage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var payState by remember { mutableStateOf("idle") }
    var payMessage by remember { mutableStateOf("") }

    var paymentMethod by remember { mutableStateOf("M-Pesa") }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (orderName.isBlank()) orderName = snapshot.child("fullname").getValue(String::class.java) ?: ""
                    if (phone.isBlank()) phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                    if (address.isBlank()) address = snapshot.child("address").getValue(String::class.java) ?: ""
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            locationMessage = "Location permission is needed"
            return
        }
        locationMessage = "Finding your location..."
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    locationMessage = "Location pinned"
                } else {
                    locationMessage = "Could not get your location. Turn on GPS and try again"
                }
            }
            .addOnFailureListener { e ->
                locationMessage = e.localizedMessage ?: "Could not get your location"
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) {
            fetchLocation()
        } else {
            locationMessage = "Location permission denied. You can still type your address"
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HawiiPrimary,
        focusedLabelColor = HawiiPrimary,
        unfocusedContainerColor = Color.White,
        focusedContainerColor = Color.White,
        cursorColor = HawiiPrimary
    )
    val pinLat = latitude
    val pinLng = longitude

    fun buildOrderMap(formattedPhone: String?, mapsLink: String): HashMap<String, Any> {
        val orderMap = hashMapOf<String, Any>(
            "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
            "orderName" to orderName.trim(),
            "phone" to (formattedPhone ?: phone.trim()),
            "address" to address.trim(),
            "mapsLink" to mapsLink,
            "notes" to CartManager.notes.trim(),
            "items" to cartItems.map {
                mapOf("foodId" to it.food.id, "name" to it.food.name, "price" to it.food.price, "quantity" to it.quantity)
            },
            "total" to total,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to "Pending",
            "orderStatus" to "Payment Pending",
            "estimatedTimeMinutes" to 30,
            "createdAt" to System.currentTimeMillis()
        )
        if (pinLat != null && pinLng != null) {
            orderMap["latitude"] = pinLat
            orderMap["longitude"] = pinLng
        }
        return orderMap
    }

    Column(modifier = Modifier.fillMaxSize().background(HawiiCream).statusBarsPadding().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HawiiDark)
            }
            Text("Checkout", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Order Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    cartItems.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text(item.quantity.toString() + " x " + item.food.name, fontSize = 14.sp, color = HawiiDark, modifier = Modifier.weight(1f))
                            Text("KES " + "%,d".format(item.food.price * item.quantity), fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                    if (CartManager.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Note: " + CartManager.notes, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            SectionTitle("Order Name")
            OutlinedTextField(
                value = orderName,
                onValueChange = { orderName = it },
                placeholder = { Text("Name for this order") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("Delivery Location")
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("Estate, street, building, landmark...") },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📍 Use my location", fontSize = 12.sp)
                }
                Button(
                    onClick = { openInGoogleMaps(context, address, pinLat, pinLng) },
                    enabled = address.isNotBlank() || pinLat != null,
                    colors = ButtonDefaults.buttonColors(containerColor = HawiiDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🗺 Open in Google Maps", fontSize = 12.sp)
                }
            }
            if (locationMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(locationMessage, fontSize = 12.sp, color = Color.Gray)
            }
            if (pinLat != null && pinLng != null) {
                Text("📍 Pinned: " + "%.5f".format(pinLat) + ", " + "%.5f".format(pinLng), fontSize = 12.sp, color = MpesaGreen)
            }

            SectionTitle("Payment Method")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { paymentMethod = "M-Pesa" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentMethod == "M-Pesa") MpesaGreen else Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("M-Pesa", color = if (paymentMethod == "M-Pesa") Color.White else HawiiDark, fontSize = 14.sp)
                }
                Button(
                    onClick = { paymentMethod = "Card" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentMethod == "Card") PaypalBlue else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Card / PayPal", color = if (paymentMethod == "Card") Color.White else HawiiDark, fontSize = 14.sp)
                }
            }

            if (paymentMethod == "M-Pesa") {
                SectionTitle("M-Pesa Phone Number")
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("e.g. 0712345678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("M-Pesa", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MpesaGreen)
                        Text("You will get a payment prompt on your phone. Enter your M-Pesa PIN to pay.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                SectionTitle("Card Details")
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    placeholder = { Text("Name on card") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 19) cardNumber = it },
                    placeholder = { Text("Card number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { if (it.length <= 5) cardExpiry = it },
                        placeholder = { Text("MM/YY") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cardCvv,
                        onValueChange = { if (it.length <= 4) cardCvv = it },
                        placeholder = { Text("CVV") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Card / PayPal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PaypalBlue)
                        Text("This is a demo payment form for the project. No real card network or PayPal account is charged.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(error, fontSize = 13.sp, color = HawiiPrimary)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                Text("KES " + "%,d".format(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = HawiiPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (cartItems.isEmpty()) {
                        error = "Your cart is empty"
                    } else if (orderName.isBlank()) {
                        error = "Enter a name for the order"
                    } else if (address.isBlank()) {
                        error = "Enter your delivery location"
                    } else if (paymentMethod == "M-Pesa") {
                        val formatted = formatPhone(phone)
                        if (formatted == null) {
                            error = "Enter a valid Safaricom number, e.g. 0712345678"
                        } else {
                            error = ""
                            payState = "sending"
                            payMessage = "Contacting M-Pesa..."

                            val mapsLink = if (pinLat != null && pinLng != null) {
                                "https://www.google.com/maps/search/?api=1&query=" + pinLat + "," + pinLng
                            } else {
                                "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address.trim())
                            }
                            val orderMap = buildOrderMap(formatted, mapsLink)

                            val ordersRef = FirebaseDatabase.getInstance().getReference("Orders")
                            val newOrderRef = ordersRef.push()
                            val orderId = newOrderRef.key

                            if (orderId == null) {
                                payState = "failed"
                                payMessage = "Could not create the order"
                            } else {
                                newOrderRef.setValue(orderMap).addOnSuccessListener {
                                    val reference = "HW" + orderId.filter { it.isLetterOrDigit() }.takeLast(8)

                                    scope.launch {
                                        delay(3000)
                                        val (checkoutId, msg) = startStkPush(formatted, total, reference)
                                        if (checkoutId == null) {
                                            newOrderRef.updateChildren(mapOf("paymentStatus" to "Failed"))
                                            payState = "failed"
                                            payMessage = msg
                                            return@launch
                                        }

                                        payState = "waiting"
                                        payMessage = "A payment request of KES " + "%,d".format(total) + " was sent to " + phone.trim() + ". Enter your M-Pesa PIN on your phone to complete the payment."

                                        var result = "pending"
                                        var attempts = 0
                                        while (result == "pending" && attempts < 30) {
                                            delay(4000)
                                            result = checkStkStatus(checkoutId)
                                            attempts++
                                        }

                                        when {
                                            result == "success" -> {
                                                newOrderRef.updateChildren(mapOf("paymentStatus" to "Paid"))
                                                payState = "success"
                                                payMessage = "Your order has been placed. Order ID: " + reference
                                            }
                                            result.startsWith("failed") -> {
                                                newOrderRef.updateChildren(mapOf("paymentStatus" to "Failed"))
                                                payState = "failed"
                                                payMessage = result.removePrefix("failed:")
                                            }
                                            else -> {
                                                newOrderRef.updateChildren(mapOf("paymentStatus" to "Unconfirmed"))
                                                payState = "failed"
                                                payMessage = "We did not get a confirmation from M-Pesa. If you were charged, keep your M-Pesa message and quote order " + reference
                                            }
                                        }
                                    }
                                }.addOnFailureListener { e ->
                                    payState = "failed"
                                    payMessage = e.localizedMessage ?: "Could not save your order"
                                }
                            }
                        }
                    } else {
                        val digitsOnly = cardNumber.filter { it.isDigit() }
                        if (cardName.isBlank()) {
                            error = "Enter the name on the card"
                        } else if (digitsOnly.length < 13) {
                            error = "Enter a valid card number"
                        } else if (!cardExpiry.matches(Regex("\\d{2}/\\d{2}"))) {
                            error = "Enter expiry as MM/YY"
                        } else if (cardCvv.length < 3) {
                            error = "Enter a valid CVV"
                        } else {
                            error = ""
                            payState = "sending"
                            payMessage = "Processing payment..."

                            val mapsLink = if (pinLat != null && pinLng != null) {
                                "https://www.google.com/maps/search/?api=1&query=" + pinLat + "," + pinLng
                            } else {
                                "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address.trim())
                            }
                            val orderMap = buildOrderMap(null, mapsLink)

                            val ordersRef = FirebaseDatabase.getInstance().getReference("Orders")
                            val newOrderRef = ordersRef.push()
                            val orderId = newOrderRef.key

                            if (orderId == null) {
                                payState = "failed"
                                payMessage = "Could not create the order"
                            } else {
                                newOrderRef.setValue(orderMap).addOnSuccessListener {
                                    val reference = "HW" + orderId.filter { it.isLetterOrDigit() }.takeLast(8)
                                    scope.launch {
                                        delay(2500)
                                        newOrderRef.updateChildren(mapOf("paymentStatus" to "Paid"))
                                        payState = "success"
                                        payMessage = "Your order has been placed. Order ID: " + reference
                                    }
                                }.addOnFailureListener { e ->
                                    payState = "failed"
                                    payMessage = e.localizedMessage ?: "Could not save your order"
                                }
                            }
                        }
                    }
                },
                enabled = payState == "idle",
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (paymentMethod == "M-Pesa") MpesaGreen else PaypalBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    (if (paymentMethod == "M-Pesa") "Pay with M-Pesa • KES " else "Pay with Card • KES ") + "%,d".format(total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (payState != "idle") {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    when (payState) {
                        "success" -> "Payment received 🎉"
                        "failed" -> "Payment not completed"
                        else -> "Payment"
                    }
                )
            },
            text = {
                Column {
                    if (payState == "sending" || payState == "waiting") {
                        CircularProgressIndicator(color = if (paymentMethod == "M-Pesa") MpesaGreen else PaypalBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(payMessage)
                }
            },
            confirmButton = {
                if (payState == "success") {
                    TextButton(onClick = {
                        CartManager.clear()
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_HOME) { inclusive = true }
                        }
                    }) {
                        Text("Back to Home", color = HawiiPrimary)
                    }
                } else if (payState == "failed") {
                    TextButton(onClick = { payState = "idle" }) {
                        Text("Try Again", color = HawiiPrimary)
                    }
                } else if (payState == "waiting") {
                    TextButton(onClick = {
                        payState = "success"
                        payMessage = "Marked as paid manually — M-Pesa sandbox didn't confirm in time."
                    }) {
                        Text("I've paid (sandbox is slow)", color = HawiiPrimary)
                    }
                }
            }
        )
    }
}