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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neighborhoodreports.R
import com.example.neighborhoodreports.auth.RegisterViewModel
import com.example.neighborhoodreports.ui.theme.PrimaryBlue

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {

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
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
            label = {
                Text(
                    "שם מלא",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // שדה אימייל
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

        // שדה סיסמה
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

        Spacer(Modifier.height(16.dp))

        // בחירת תפקיד
        Text(
            text = "בחר/י תפקיד",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            RadioButton(
                selected = uiState.role == "user",
                onClick = { viewModel.updateRole("user") }
            )
            Text("משתמש", modifier = Modifier.padding(start = 8.dp))

            Spacer(Modifier.width(24.dp))

            RadioButton(
                selected = uiState.role == "admin",
                onClick = { viewModel.updateRole("admin") }
            )
            Text("מנהל", modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(Modifier.height(24.dp))

        // כפתור הרשמה
        Button(
            onClick = { viewModel.register() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("הרשמה")
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
            navController.navigate("login")
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("כבר יש לך חשבון? התחבר/י כאן")
        }
    }
}
