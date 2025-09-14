package com.carecomms.android.ui.theme

import androidx.compose.ui.graphics.Color

// CareComms Color Palette - Deep purple, light purple, and white
val DeepPurple = Color(0xFF4A148C)
val LightPurple = Color(0xFF9C27B0)
val LightPurpleVariant = Color(0xFFE1BEE7)
val White = Color(0xFFFFFFFF)
val LightGray = Color(0xFFF5F5F5)
val DarkGray = Color(0xFF424242)
val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF388E3C)

// Text colors for accessibility
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
val TextOnPrimary = Color(0xFFFFFFFF)

// High contrast color palette (WCAG AA compliant)
object HighContrastColors {
    val Primary = Color(0xFF000000) // Pure black for maximum contrast
    val Secondary = Color(0xFF0066CC) // High contrast blue
    val Background = Color(0xFFFFFFFF) // Pure white
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFCC0000) // High contrast red
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)
    val OnError = Color(0xFFFFFFFF)
    
    // Additional high contrast colors
    val HighContrastBorder = Color(0xFF000000)
    val HighContrastFocus = Color(0xFF0066CC)
    val HighContrastDisabled = Color(0xFF666666)
}