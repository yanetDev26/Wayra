package com.app.wayra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Traduce la paleta de Wayra a los roles de Material 3, para que los
 * componentes de la librería (TextField, AlertDialog, Menu, Snackbar) queden
 * dentro del sistema en vez de recurrir a sus grises azulados por defecto.
 */
private fun WayraColors.toMaterialScheme() = with(this) {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    base.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandSoft,
        onPrimaryContainer = brandDeep,

        secondary = brandDeep,
        onSecondary = onBrand,
        secondaryContainer = surfaceAlt,
        onSecondaryContainer = ink,

        tertiary = info,
        onTertiary = onBrand,
        tertiaryContainer = infoSoft,
        onTertiaryContainer = info,

        background = paper,
        onBackground = ink,

        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceAlt,
        onSurfaceVariant = inkMuted,

        surfaceContainerLowest = surface,
        surfaceContainerLow = paper,
        surfaceContainer = surfaceAlt,
        surfaceContainerHigh = surfaceAlt,
        surfaceContainerHighest = surfaceAlt,
        surfaceBright = surface,
        surfaceDim = surfaceAlt,

        inverseSurface = hero,
        inverseOnSurface = onHero,

        error = danger,
        onError = onBrand,
        errorContainer = dangerSoft,
        onErrorContainer = danger,

        outline = border,
        outlineVariant = surfaceAlt,

        // Sin tinte de elevación: las superficies se mantienen limpias.
        surfaceTint = Color.Transparent,
        scrim = Color(0xCC0B0F12)
    )
}

@Composable
fun WayraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) NocturnaColors else PistaColors
    CompositionLocalProvider(LocalWayraColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = Typography,
            shapes = WayraShapes,
            content = content
        )
    }
}
