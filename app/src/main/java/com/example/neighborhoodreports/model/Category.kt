package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

data class Category(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
