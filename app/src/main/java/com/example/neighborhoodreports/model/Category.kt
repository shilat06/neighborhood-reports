package com.example.neighborhoodreports.model

import com.google.firebase.Timestamp

/**
 * מודל המייצג קטגוריה לדיווחים.
 * משמש למיון וסינון דיווחים לפי נושאים.
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
