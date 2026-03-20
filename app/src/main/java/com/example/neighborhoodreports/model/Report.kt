package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

/**
 * מודל המייצג דיווח השייך למשתמש מסוים. מכיל את תוכן הדיווח, מזהה הקטגוריה שלו והסטטוס הנוכחי
 * (ממתין לאישור, מאושר או נדחה).
 */
data class Report(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val categoryId: String = "",
        val imageUrl: String = "",
        val userId: String = "",
        val longitude: Double = 0.0,
        val latitude: Double = 0.0,
        val address: String = "",
        val status: String = "pending", // pending / approved / rejected / deleted
        val commentCount: Int = 0,
        val createdAt: Timestamp = Timestamp.now()
)
