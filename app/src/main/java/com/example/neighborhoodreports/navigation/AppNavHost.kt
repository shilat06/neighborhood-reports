package com.example.neighborhoodreports.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neighborhoodreports.data.SessionManager
import com.example.neighborhoodreports.ui.auth.LoginScreen
import com.example.neighborhoodreports.ui.auth.RegisterScreen
import com.example.neighborhoodreports.ui.home.HomeScreen
import com.example.neighborhoodreports.ui.admin.AdminScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.neighborhoodreports.data.UserRepository

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val repo = remember { UserRepository() }

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val uid = session.getUser()

        startDestination = if (uid != null && repo.isUserExists(uid)) {
            "home"
        } else {
            session.clear()
            "login"
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable("login") {
            LoginScreen(navController = navController, session = session)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            HomeScreen()
        }
        composable("admin") {
            AdminScreen()
        }
    }
}
