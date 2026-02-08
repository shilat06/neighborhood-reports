package com.example.neighborhoodreports.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neighborhoodreports.ui.auth.LoginScreen
import com.example.neighborhoodreports.ui.auth.RegisterScreen
import com.example.neighborhoodreports.ui.home.HomeScreen
import com.example.neighborhoodreports.ui.admin.AdminScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("home") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen()
        }

        composable("admin") {
            AdminScreen()
        }
    }
}
