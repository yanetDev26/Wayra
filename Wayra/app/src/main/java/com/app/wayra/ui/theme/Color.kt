package com.app.wayra.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de Wayra.
 *
 * Los nombres son semánticos: describen el rol del color, no su tono. Para
 * cambiar la identidad de la app basta con ajustar los valores de esta sección
 * sin tocar las pantallas.
 */

// ---------------------------------------------------------------------------
// Marca
// ---------------------------------------------------------------------------

/** Naranja principal. Barras superiores, botones primarios y acentos. */
val WayraOrange = Color(0xFFD95F18)

/** Naranja profundo para estados presionados y textos sobre fondos claros. */
val WayraOrangeDark = Color(0xFFAF4810)

/** Naranja claro para acentos sobre superficies oscuras (barra inferior). */
val WayraOrangeLight = Color(0xFFF08A3C)

/** Fondo tenue de marca para chips, badges y contenedores destacados. */
val WayraOrangeSoft = Color(0xFFFBEDE2)

// ---------------------------------------------------------------------------
// Neutros cálidos
// ---------------------------------------------------------------------------

/** Fondo general de la app. Blanco cálido, hereda el tono crema original. */
val Paper = Color(0xFFFAF6F0)

/** Superficie de tarjetas y hojas. */
val SurfaceCard = Color(0xFFFFFFFF)

/** Relleno sutil: campos de búsqueda, filas alternas, contenedores planos. */
val SurfaceAlt = Color(0xFFF3EEE6)

/** Superficie oscura para la barra inferior y bloques de contraste. */
val SurfaceDark = Color(0xFF201D1A)

/** Líneas divisorias y bordes de tarjeta. */
val BorderSoft = Color(0xFFE9E2D7)

// ---------------------------------------------------------------------------
// Texto
// ---------------------------------------------------------------------------

/** Texto principal sobre superficies claras. */
val Ink = Color(0xFF1F1C19)

/** Texto secundario: descripciones, metadatos, etiquetas. */
val InkMuted = Color(0xFF6B6259)

/** Texto terciario: placeholders, ayudas, estados vacíos. */
val InkSubtle = Color(0xFF9A9188)

/** Texto e iconos sobre naranja o sobre superficies oscuras. */
val OnDark = Color(0xFFFDFAF5)

// ---------------------------------------------------------------------------
// Estados
// ---------------------------------------------------------------------------

val Success = Color(0xFF2F7D5B)
val SuccessSoft = Color(0xFFE7F3ED)

val Warning = Color(0xFFB4741A)
val WarningSoft = Color(0xFFFBF1E0)

val Danger = Color(0xFFC0392B)
val DangerSoft = Color(0xFFFAEBE9)

val Info = Color(0xFF2C6E8F)
val InfoSoft = Color(0xFFE9F1F5)

/** Gris neutro para indicadores inactivos. */
val Neutral = Color(0xFF9A9188)
