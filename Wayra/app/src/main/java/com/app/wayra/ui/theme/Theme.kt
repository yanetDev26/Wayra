package com.app.wayra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WayraOrange,
    onPrimary = Cream,
    primaryContainer = WayraOrange,
    onPrimaryContainer = Cream,

    secondary = WayraOrange,
    onSecondary = Cream,
    secondaryContainer = WayraOrange,
    onSecondaryContainer = Cream,

    tertiary = WayraOrange,
    onTertiary = Cream,

    background = Black ,
    onBackground = Cream,

    surface = Black,
    onSurface = Cream,
    surfaceVariant = Black,
    onSurfaceVariant = Cream,

    error = Color(0xFFCF6679),
    onError = Black,

    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A)
)

@Composable
fun WayraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
