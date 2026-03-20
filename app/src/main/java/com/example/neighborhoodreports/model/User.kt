package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

/**
 * מודל המייצג משתמש במערכת השכונתית.
 * שומר את פרטי המשתמש, כתובת האימייל וההרשאות שלו (תפקיד).
 */
data class User(
        val uid: String = "",
        val email: String = "",
        val name: String = "",
        val role: String = "user", // user / admin
        val avatarUrl: String = "",
        val fcmToken: String = "",
        val createdAt: Timestamp = Timestamp.now()
)
