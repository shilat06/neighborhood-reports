package com.example.neighborhoodreports.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.neighborhoodreports.R

val Heebo = FontFamily(
    Font(R.font.heebo_regular, FontWeight.Normal),
    Font(R.font.heebo_medium, FontWeight.Medium),
    Font(R.font.heebo_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = Heebo,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Heebo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Heebo,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    )
)
