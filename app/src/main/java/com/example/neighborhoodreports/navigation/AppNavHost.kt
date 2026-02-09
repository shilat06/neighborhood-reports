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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.neighborhoodreports.data.repository.UserRepository
import com.example.neighborhoodreports.ui.screens.AdminScreen
import com.example.neighborhoodreports.ui.screens.HomeScreen
import com.example.neighborhoodreports.ui.screens.NewReportScreen
import com.example.neighborhoodreports.ui.screens.ReportDetailsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val repo = remember { UserRepository() }

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

        val uid = session.getUser()
        if (uid == null) {
            startDestination = "login"
            return@LaunchedEffect
        }
        // בדיקה אם המשתמש קיים ב-Firestore
        val user = repo.getUser(uid)
        if (user == null) {
            session.clear()
            startDestination = "login"
            return@LaunchedEffect
        }
        //בדיקת ROLE
        startDestination = when (user.role) {
            "admin" -> "admin"
            else -> "home"
        }

    }
    //מסך טעינה עד שהstartDestination נקבע
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController, startDestination = startDestination!!
    ) {
        composable("login") {
            LoginScreen(navController = navController, session = session)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController)
        }

        composable("admin") {
            AdminScreen()
        }
        composable("newReport") {
            NewReportScreen(onReportAdded = { navController.popBackStack() })
        }
        composable("details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ReportDetailsScreen(navController, id)
        }

    }
}
