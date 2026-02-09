package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrl: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userId: String = "",
    val status: String = "pending", // pending / approved / rejected
    val createdAt: Timestamp = Timestamp.now()
)
