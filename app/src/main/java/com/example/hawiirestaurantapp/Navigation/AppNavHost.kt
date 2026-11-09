package com.example.hawiirestaurantapp.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hawiirestaurantapp.Screens.Admin.AdminDashboardScreen
import com.example.hawiirestaurantapp.Screens.Admin.AdminOrdersScreen
import com.example.hawiirestaurantapp.Screens.Admin.ProductFormScreen
import com.example.hawiirestaurantapp.Screens.Cart.CartScreen
import com.example.hawiirestaurantapp.Screens.Checkout.CheckoutScreen
import com.example.hawiirestaurantapp.Screens.Home.FoodDetailScreen
import com.example.hawiirestaurantapp.Screens.Home.HomeScreen
import com.example.hawiirestaurantapp.Screens.Login.LoginScreen
import com.example.hawiirestaurantapp.Screens.Profile.ProfileScreen
import com.example.hawiirestaurantapp.Screens.Signup.SignupScreen
import com.example.hawiirestaurantapp.Screens.orders.UserOrdersScreen
import com.example.hawiirestaurantapp.Screens.splashscreen.splashscreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
){
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_SPLASH) {
            splashscreen(navController)
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_SIGNUP) {
            SignupScreen(navController)
        }
        composable(ROUTE_HOME) {
            HomeScreen(navController)
        }
        composable(ROUTE_ADMIN) {
            AdminDashboardScreen(navController)
        }
        composable(ROUTE_ADMIN_ORDERS) { 
            AdminOrdersScreen(navController)
        }
        composable(ROUTE_CART) {
            CartScreen(navController)
        }
        composable(ROUTE_CHECKOUT) {
            CheckoutScreen(navController)
        }
        composable(ROUTE_PROFILE) {
            ProfileScreen(navController)
        }
        composable(ROUTE_USER_ORDERS) {
            UserOrdersScreen(navController)
        }
        composable(
            "food/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            FoodDetailScreen(navController, backStackEntry.arguments?.getString("foodId") ?: "")
        }
        composable(
            "productform/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            ProductFormScreen(navController, backStackEntry.arguments?.getString("productId") ?: "new")
        }
    }
}