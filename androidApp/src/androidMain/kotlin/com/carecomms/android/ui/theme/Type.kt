package com.carecomms.android.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carecomms.accessibility.AccessibilityUtils

// Typography optimized for elderly users with larger text sizes
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Larger than default for accessibility
        lineHeight = 24.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    h4 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    h5 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    h6 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
)
/**
 * C
reates scaled typography based on user accessibility preferences
 */
fun getScaledTypography(textScale: Float): Typography {
    return Typography(
        body1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = AccessibilityUtils.getScaledTextSize(18f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(24f, textScale).sp
        ),
        body2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = AccessibilityUtils.getScaledTextSize(16f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(22f, textScale).sp
        ),
        button = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.W500,
            fontSize = AccessibilityUtils.getScaledTextSize(16f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(20f, textScale).sp
        ),
        caption = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = AccessibilityUtils.getScaledTextSize(14f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(18f, textScale).sp
        ),
        h1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(32f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(40f, textScale).sp
        ),
        h2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(28f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(36f, textScale).sp
        ),
        h3 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(24f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(32f, textScale).sp
        ),
        h4 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(20f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(28f, textScale).sp
        ),
        h5 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(18f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(24f, textScale).sp
        ),
        h6 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = AccessibilityUtils.getScaledTextSize(16f, textScale).sp,
            lineHeight = AccessibilityUtils.getScaledTextSize(22f, textScale).sp
        )
    )
}