package com.example.neighborhoodreports.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.repository.AuthRepository
import com.example.neighborhoodreports.data.SessionManager
import com.example.neighborhoodreports.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepo: AuthRepository = AuthRepository(),   // אחראי על FirebaseAuth
    private val userRepo: UserRepository = UserRepository(),     // אחראי על Firestore (role + פרטי משתמש)
) : ViewModel() {

    // מצב המסך (UI State)
    var uiState by mutableStateOf(LoginUiState())
        private set

    // פונקציית התחברות
    fun login(session: SessionManager) {
        viewModelScope.launch {

            // עדכון מצב: טוען, איפוס שגיאה
            uiState = uiState.copy(loading = true, error = null)

            try {
                // שלב 1: התחברות ל-FirebaseAuth וקבלת UID
                val uid = authRepo.login(uiState.email, uiState.password)

                // שלב 2: שליפת פרטי המשתמש מה-DB כולל role
                val user = userRepo.getUser(uid)
                session.debugPrint()

                session.saveUser(uid)
                // עדכון מצב: הצלחה + שמירת role
                uiState = uiState.copy(
                    loading = false,
                    success = true,
                    role = user?.role
                )

            } catch (e: Exception) {
                // במקרה של שגיאה: עדכון הודעת שגיאה
                uiState = uiState.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    // עדכון אימייל
    fun updateEmail(value: String) {
        uiState = uiState.copy(email = value)
    }

    // עדכון סיסמה
    fun updatePassword(value: String) {
        uiState = uiState.copy(password = value)
    }
}

// מצב המסך (State Holder)
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val role: String? = null,
)
