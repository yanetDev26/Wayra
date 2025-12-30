package com.app.wayra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.wayra.R

// Tipografía Agenor Neue
val AgenorNeue = FontFamily(
    Font(R.font.agenor_neue_regular, FontWeight.Normal)
)

val Typography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    displayMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    displaySmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    headlineMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    headlineSmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    // Title styles
    titleLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = White
    ),
    titleMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = White
    ),
    titleSmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = White
    ),
    // Body styles
    bodyLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = White
    ),
    bodyMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = White
    ),
    bodySmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = White
    ),
    // Label styles
    labelLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = White
    ),
    labelMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = White
    ),
    labelSmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = White
    )
)
