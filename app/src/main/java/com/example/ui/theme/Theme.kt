package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Navy900,
    primaryContainer = NavyDarkContainer,
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = SaffronOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF422006),
    onSecondaryContainer = Color(0xFFFFD8A8),
    tertiary = Color(0xFF81C784),
    onTertiary = Color.Black,
    error = SosDangerRed,
    onError = Color.White,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF16253B),
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = Color.White,
    primaryContainer = NavyLightContainer,
    onPrimaryContainer = Navy900,
    secondary = SaffronAlertDark,
    onSecondary = Color.White,
    secondaryContainer = SaffronAlertLight,
    onSecondaryContainer = Color(0xFF7A2700),
    tertiary = SafetyGreenAccent,
    onTertiary = Color.White,
    error = SosDangerRed,
    onError = Color.White,
    background = OffWhiteBg,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

@Composable
fun DhritiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
