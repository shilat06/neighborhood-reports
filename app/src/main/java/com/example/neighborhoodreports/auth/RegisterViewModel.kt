package com.example.neighborhoodreports.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.AuthRepository
import com.example.neighborhoodreports.data.UserRepository
import com.example.neighborhoodreports.data.AppUser
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun register() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)

            try {
                // שלב 1: יצירת משתמש ב-FirebaseAuth
                val uid = authRepo.register(uiState.email, uiState.password)

                // שלב 2: שמירת פרטי המשתמש ב-Firestore כולל role
                val user = AppUser(
                    uid = uid,
                    email = uiState.email,
                    role = uiState.role
                )
                userRepo.createUser(user)

                uiState = uiState.copy(loading = false, success = true)

            } catch (e: Exception) {
                uiState = uiState.copy(loading = false, error = e.message)
            }
        }
    }

    fun updateEmail(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun updatePassword(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun updateRole(value: String) {
        uiState = uiState.copy(role = value)
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val role: String = "user",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
