package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user", // user / admin
    val createdAt: Timestamp = Timestamp.now()
)