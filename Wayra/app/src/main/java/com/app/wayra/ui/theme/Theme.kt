package com.app.wayra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WayraOrange,
    onPrimary = White,
    primaryContainer = OrangeDark,
    onPrimaryContainer = White,

    secondary = WayraOrange,
    onSecondary = White,
    secondaryContainer = OrangeDark,
    onSecondaryContainer = White,

    tertiary = WayraOrange,
    onTertiary = White,

    background = Black,
    onBackground = White,

    surface = SurfaceBlack,
    onSurface = White,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = White,

    error = Color(0xFFCF6679),
    onError = Black,

    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A)
)

@Composable
fun WayraTheme(
    darkTheme: Boolean = true, // Siempre usar tema oscuro
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
