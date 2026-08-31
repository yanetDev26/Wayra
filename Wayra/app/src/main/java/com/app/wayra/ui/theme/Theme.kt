package com.app.wayra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * La app se dibuja siempre sobre fondo claro, por lo que el esquema es claro.
 * Antes se usaba [androidx.compose.material3.darkColorScheme], lo que invertía
 * los roles `on*` y dejaba textos ilegibles en los componentes que confían en
 * `MaterialTheme.colorScheme`.
 */
private val WayraColorScheme = lightColorScheme(
    primary = WayraOrange,
    onPrimary = OnDark,
    primaryContainer = WayraOrangeSoft,
    onPrimaryContainer = WayraOrangeDark,

    secondary = WayraOrangeDark,
    onSecondary = OnDark,
    secondaryContainer = SurfaceAlt,
    onSecondaryContainer = Ink,

    tertiary = Info,
    onTertiary = OnDark,
    tertiaryContainer = InfoSoft,
    onTertiaryContainer = Info,

    background = Paper,
    onBackground = Ink,

    surface = SurfaceCard,
    onSurface = Ink,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = InkMuted,

    // Roles de contenedor: los usan TextField, Menu, AlertDialog y NavigationBar.
    // Sin definirlos, Material recurre a sus grises azulados por defecto.
    surfaceContainerLowest = SurfaceCard,
    surfaceContainerLow = Paper,
    surfaceContainer = SurfaceAlt,
    surfaceContainerHigh = SurfaceAlt,
    surfaceContainerHighest = SurfaceAlt,
    surfaceBright = SurfaceCard,
    surfaceDim = SurfaceAlt,

    // Sin tinte de elevación: las tarjetas se mantienen blancas y limpias.
    surfaceTint = Color.Transparent,

    inverseSurface = SurfaceDark,
    inverseOnSurface = OnDark,

    error = Danger,
    onError = OnDark,
    errorContainer = DangerSoft,
    onErrorContainer = Danger,

    outline = BorderSoft,
    outlineVariant = SurfaceAlt,

    scrim = Ink
)

@Composable
fun WayraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WayraColorScheme,
        typography = Typography,
        shapes = WayraShapes,
        content = content
    )
}
