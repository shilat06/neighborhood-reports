package com.example.neighborhoodreports.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neighborhoodreports.R
import com.example.neighborhoodreports.auth.LoginViewModel
import com.example.neighborhoodreports.data.SessionManager
import com.example.neighborhoodreports.ui.theme.PrimaryBlue

@Composable
fun LoginScreen(
    navController: NavController,
    session: SessionManager,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(60.dp))

        Icon(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
            tint = PrimaryBlue
        )

        Spacer(Modifier.height(40.dp))

        TextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
            label = {
                Text(
                    "אימייל",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_email),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
            label = {
                Text(
                    "סיסמה",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_password),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(session) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("התחברות")
        }

        if (uiState.loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        uiState.error?.let {
            Spacer(Modifier.height(16.dp))
            Text(text = it, color = Color.Red)
        }

        if (uiState.success) {
            when (uiState.role) {
                "admin" -> {
                    navController.navigate("admin") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                "user" -> {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }


        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("אין לך חשבון? הירשמי כאן")
        }
    }
}

