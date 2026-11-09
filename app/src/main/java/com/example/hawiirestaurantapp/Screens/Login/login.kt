package com.example.hawiirestaurantapp.Screens.Login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.hawiirestaurantapp.Navigation.ROUTE_ADMIN
import com.example.hawiirestaurantapp.Navigation.ROUTE_HOME
import com.example.hawiirestaurantapp.Navigation.ROUTE_LOGIN
import com.example.hawiirestaurantapp.Navigation.ROUTE_SIGNUP
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiCream
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiDark
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiPrimary
import com.example.hawiirestaurantapp.Screens.splashscreen.HawiiSecondary
import com.example.hawiirestaurantapp.Screens.viewmodel.AuthViewModel


@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(HawiiCream).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(
                Brush.linearGradient(listOf(HawiiPrimary, HawiiSecondary)), CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Text("🍴", fontSize = 38.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = HawiiDark)) { append("Haw") }
                withStyle(SpanStyle(color = HawiiPrimary)) { append("ii") }
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = HawiiDark)
        Text("Login to order your favourite food", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HawiiPrimary,
                focusedLabelColor = HawiiPrimary,
                cursorColor = HawiiPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show", color = HawiiPrimary)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HawiiPrimary,
                focusedLabelColor = HawiiPrimary,
                cursorColor = HawiiPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(viewModel.message, color = HawiiPrimary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                viewModel.login(email, password) { isAdmin ->
                    navController.navigate(if (isAdmin) ROUTE_ADMIN else ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            },
            enabled = !viewModel.loading,
            colors = ButtonDefaults.buttonColors(containerColor = HawiiPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (viewModel.loading) "Logging in..." else "Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", fontSize = 14.sp, color = Color.Gray)
            Text(
                "Sign Up",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HawiiPrimary,
                modifier = Modifier.clickable { navController.navigate(ROUTE_SIGNUP) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun loginscreenpreview() {
    LoginScreen(rememberNavController())
}