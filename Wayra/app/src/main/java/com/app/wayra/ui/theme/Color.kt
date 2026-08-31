package com.app.wayra.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta de Wayra en dos temperaturas.
 *
 * "Pista" es la diurna: gris frío de fondo y tarjetas blancas. El neutro frío
 * es lo que permite reservar el naranja: sobre un crema cálido el acento se
 * funde con el fondo y deja de señalar nada.
 *
 * "Nocturna" es la misma estructura en grafito, para el entrenamiento de las
 * seis de la mañana. No es una inversión automática: los estados se aclaran
 * para mantener contraste sobre fondo oscuro, y el naranja se enciende.
 *
 * Los tokens se leen igual desde las pantallas (`Paper`, `Ink`, `Danger`…) y
 * resuelven solos según el tema activo.
 */
@Immutable
data class WayraColors(
    // Marca
    val brand: Color,
    val brandDeep: Color,
    val brandSoft: Color,
    val onBrand: Color,
    // Superficies
    val paper: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val hero: Color,
    val onHero: Color,
    val border: Color,
    // Texto
    val ink: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    // Estados
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val neutral: Color,
    /** Color de dato cuantitativo. Ver nota en [PistaColors]. */
    val dataAccent: Color,
    val dataTrack: Color,
    val isDark: Boolean
)

/** Diurna. Gris frío, tarjetas blancas, naranja como señal. */
val PistaColors = WayraColors(
    brand = Color(0xFFE2571C),
    brandDeep = Color(0xFFB8430F),
    brandSoft = Color(0xFFFDEEE6),
    onBrand = Color(0xFFFFFFFF),

    paper = Color(0xFFF1F3F5),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFE8ECEF),
    hero = Color(0xFF22303A),
    onHero = Color(0xFFF4F7F9),
    border = Color(0xFFE2E6EA),

    ink = Color(0xFF14181C),
    inkMuted = Color(0xFF626F7B),
    inkSubtle = Color(0xFF93A0AB),

    success = Color(0xFF1F7A5C),
    successSoft = Color(0xFFE7F2EE),
    warning = Color(0xFFB26A00),
    warningSoft = Color(0xFFFBF2E3),
    danger = Color(0xFFC13525),
    dangerSoft = Color(0xFFFAEBE9),
    info = Color(0xFF2C6E8F),
    infoSoft = Color(0xFFE9F1F5),
    neutral = Color(0xFF93A0AB),

    // Hue propio para magnitudes (barras de proporcion). No es el naranja,
    // que esta reservado para la accion, ni un color de estado, que leeria
    // como "bueno/malo". Validado en ambas superficies: banda de luminosidad,
    // piso de croma y contraste >= 3:1.
    dataAccent = Color(0xFF2790C0),
    dataTrack = Color(0xFFE8ECEF),
    isDark = false
)

/** Nocturna. Grafito, naranja encendido, estados aclarados para el fondo oscuro. */
val NocturnaColors = WayraColors(
    brand = Color(0xFFFF7A3D),
    brandDeep = Color(0xFFFF9463),
    brandSoft = Color(0xFF2C1B12),
    onBrand = Color(0xFF190C05),

    paper = Color(0xFF0F1418),
    surface = Color(0xFF171E24),
    surfaceAlt = Color(0xFF1E262D),
    hero = Color(0xFF202B33),
    onHero = Color(0xFFE9EEF2),
    border = Color(0xFF242D35),

    ink = Color(0xFFE9EEF2),
    inkMuted = Color(0xFF8A98A4),
    inkSubtle = Color(0xFF64717C),

    success = Color(0xFF3FA37D),
    successSoft = Color(0xFF14261F),
    warning = Color(0xFFD9A03C),
    warningSoft = Color(0xFF2A2114),
    danger = Color(0xFFE4614C),
    dangerSoft = Color(0xFF2C1815),
    info = Color(0xFF6BA8C6),
    infoSoft = Color(0xFF14212A),
    neutral = Color(0xFF64717C),
    dataAccent = Color(0xFF2790C0),
    dataTrack = Color(0xFF232C34),
    isDark = true
)

val LocalWayraColors = staticCompositionLocalOf { PistaColors }

// ---------------------------------------------------------------------------
// Tokens. Se leen como antes; ahora resuelven según el tema activo.
// ---------------------------------------------------------------------------

val WayraOrange: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.brand
val WayraOrangeDark: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.brandDeep
val WayraOrangeSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.brandSoft
/** Texto e iconos sobre el naranja. */
val OnBrand: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.onBrand

val Paper: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.paper
val SurfaceCard: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.surface
val SurfaceAlt: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.surfaceAlt
/** Superficie del bloque de recaudación. Slate, nunca negro. */
val SurfaceDark: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.hero
/** Texto sobre [SurfaceDark]. */
val OnDark: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.onHero
val BorderSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.border

val Ink: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.ink
val InkMuted: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.inkMuted
val InkSubtle: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.inkSubtle

val Success: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.success
val SuccessSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.successSoft
val Warning: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.warning
val WarningSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.warningSoft
val Danger: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.danger
val DangerSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.dangerSoft
val Info: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.info
val InfoSoft: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.infoSoft
val Neutral: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.neutral

/** Relleno de las barras de proporción. */
val DataAccent: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.dataAccent
/** Riel de las barras de proporción. */
val DataTrack: Color
    @Composable @ReadOnlyComposable get() = LocalWayraColors.current.dataTrack
