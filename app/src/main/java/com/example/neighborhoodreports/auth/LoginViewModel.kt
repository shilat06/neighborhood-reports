package com.example.neighborhoodreports.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.AuthRepository
import com.example.neighborhoodreports.data.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun login() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)

            try {
                val uid = authRepo.login(uiState.email, uiState.password)
                val user = userRepo.getUser(uid)

                uiState = uiState.copy(loading = false, success = true, role = user?.role)
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
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val role: String? = null
)
