package com.carecomms.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.carecomms.accessibility.AndroidAccessibilityPreferences

private val LightColorPalette = lightColors(
    primary = DeepPurple,
    primaryVariant = LightPurple,
    secondary = LightPurple,
    secondaryVariant = LightPurpleVariant,
    background = White,
    surface = White,
    error = ErrorRed,
    onPrimary = TextOnPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextOnPrimary
)

private val DarkColorPalette = darkColors(
    primary = LightPurple,
    primaryVariant = DeepPurple,
    secondary = LightPurpleVariant,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = ErrorRed,
    onPrimary = TextOnPrimary,
    onSecondary = TextPrimary,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onError = TextOnPrimary
)

private val HighContrastColorPalette = lightColors(
    primary = HighContrastColors.Primary,
    primaryVariant = HighContrastColors.Primary,
    secondary = HighContrastColors.Secondary,
    background = HighContrastColors.Background,
    surface = HighContrastColors.Surface,
    error = HighContrastColors.Error,
    onPrimary = HighContrastColors.OnPrimary,
    onSecondary = HighContrastColors.OnSecondary,
    onBackground = HighContrastColors.OnBackground,
    onSurface = HighContrastColors.OnSurface,
    onError = HighContrastColors.OnError
)

@Composable
fun CareCommsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val accessibilityPreferences = AndroidAccessibilityPreferences(context)
    val accessibilitySettings by accessibilityPreferences.observeAccessibilitySettings().collectAsState()
    
    val colors = when {
        accessibilitySettings.isHighContrastEnabled -> HighContrastColorPalette
        darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }
    
    val scaledTypography = getScaledTypography(accessibilitySettings.textScale)

    MaterialTheme(
        colors = colors,
        typography = scaledTypography,
        shapes = Shapes,
        content = content
    )
}