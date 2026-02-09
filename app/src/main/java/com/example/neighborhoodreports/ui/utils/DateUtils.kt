package com.example.neighborhoodreports.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: Date?): String {
    if (date == null) return ""
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("he"))
    return formatter.format(date)
}
