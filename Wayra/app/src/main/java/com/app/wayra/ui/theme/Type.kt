package com.app.wayra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.wayra.R

/** Tipografía de marca. Se reserva para títulos y cifras destacadas. */
val AgenorNeue = FontFamily(
    Font(R.font.agenor_neue_regular, FontWeight.Normal)
)

/**
 * Tipografía de lectura. Agenor Neue solo trae el peso Regular, así que en
 * textos pequeños el sistema tenía que sintetizar la negrita y los cuerpos de
 * 11-13 sp quedaban borrosos. Para body y label se usa la familia del sistema,
 * que sí tiene pesos reales. Para volver a Agenor en todo, cambiar este valor
 * por `AgenorNeue`.
 */
val WayraBodyFont = FontFamily.Default

/**
 * Escala tipográfica.
 *
 * Pasos permitidos: 12 · 14 · 16 · 18 · 20 · 22 · 24 · 28 · 32 · 40 sp.
 * Nada baja de 12 sp y cada estilo lleva su `lineHeight`, para que el ritmo
 * vertical sea el mismo en todas las pantallas.
 *
 * Los estilos no fijan `color`: lo aporta `colorScheme` según la superficie.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.1).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

/**
 * Estilos que Material no cubre y que el diseño de Wayra necesita nombrar.
 *
 * `amount` y `metric` activan cifras tabulares (`tnum`): en una columna de
 * importes los dígitos quedan alineados, que es la mitad de la legibilidad de
 * una pantalla de cobros.
 */
object WayraType {

    /** Rótulo de sección. Versalita espaciada, para ordenar sin sumar cajas. */
    val sectionLabel = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.1.sp
    )

    /** Etiqueta de estado dentro de un badge. */
    val badge = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp
    )

    /** Importes en listas y filas de detalle. */
    val amount = TextStyle(
        fontFamily = WayraBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    )

    /** Cifra destacada de una métrica. Tipografía de marca. */
    val metric = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    )

    /** Cifra principal de la pantalla de inicio. */
    val metricHero = TextStyle(
        fontFamily = AgenorNeue,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp,
        fontFeatureSettings = "tnum"
    )
}
