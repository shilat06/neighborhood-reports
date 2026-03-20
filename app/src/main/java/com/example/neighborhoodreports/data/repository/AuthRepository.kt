package com.example.neighborhoodreports.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * אחראי על ניהול משתמשים מול מערכת Firebase Authentication.
 * מספק פונקציות להתחברות, הרשמה, התנתקות ושינוי סיסמה.
 */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /**
     * הרשמת משתמש חדש
     */
    suspend fun register(email: String, password: String): String {

        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Registration failed")
    }

    /**
     * התחברות לחשבון קיים
     */
    suspend fun login(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    /**
     * התנתקות מהמערכת
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * מחזיר את המזהה (UID) של המשתמש המחובר, או null אם לא מחובר
     */
    fun currentUserId(): String? = auth.currentUser?.uid

    /** 
     * אימות מחדש של המשתמש ועדכון לסיסמה חדשה. 
     * זורק שגיאה במקרה של כישלון. 
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: throw Exception("לא מחובר")
        val email = user.email ?: throw Exception("אין אימייל מקושר")
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }
}
