package com.example.neighborhoodreports.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.AppUser
import com.example.neighborhoodreports.data.AuthRepository
import com.example.neighborhoodreports.data.UserRepository
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
                val uid = authRepo.register(uiState.email, uiState.password)

                val user = AppUser(
                    uid = uid,
                    email = uiState.email,
                    role = "user"
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

    fun updateConfirmPassword(value: String) {
        uiState = uiState.copy(confirmPassword = value)
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
