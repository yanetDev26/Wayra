package com.app.wayra.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Colores de la barra superior, en un solo lugar para que todas las pantallas
 * compartan el mismo encabezado.
 *
 * El encabezado es claro en vez de una banda naranja: la marca vive en la
 * acción principal y en la pestaña activa, y así el acento sigue significando
 * algo. De paso, la barra de estado puede usar iconos oscuros en toda la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun wayraTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Paper,
    scrolledContainerColor = Paper,
    titleContentColor = Ink,
    navigationIconContentColor = InkMuted,
    actionIconContentColor = InkMuted
)

/** Tono semántico de un estado. El color codifica severidad, no decora. */
enum class Tone { Success, Warning, Danger, Neutral, Brand }

@Composable
private fun Tone.fg(): Color = when (this) {
    Tone.Success -> Success
    Tone.Warning -> Warning
    Tone.Danger -> Danger
    Tone.Neutral -> InkMuted
    Tone.Brand -> WayraOrangeDark
}

@Composable
private fun Tone.bg(): Color = when (this) {
    Tone.Success -> SuccessSoft
    Tone.Warning -> WarningSoft
    Tone.Danger -> DangerSoft
    Tone.Neutral -> SurfaceAlt
    Tone.Brand -> WayraOrangeSoft
}

/**
 * Etiqueta de estado. Fondo tenue y texto de color, en vez de una píldora
 * saturada: se lee igual, no compite con la acción principal y no necesita
 * invertir el texto al cambiar de tema.
 */
@Composable
fun StatusBadge(
    text: String,
    tone: Tone,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(tone.bg(), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = WayraType.badge,
            color = tone.fg()
        )
    }
}

/** Punto de estado para las listas, donde el texto sería ruido. */
@Composable
fun StatusDot(tone: Tone, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(7.dp)
            .background(tone.fg(), CircleShape)
    )
}

/**
 * Rótulo de sección: versalita espaciada. Ordena la pantalla sin sumar una
 * caja más ni gastar el acento.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = WayraType.sectionLabel,
        color = InkSubtle,
        modifier = modifier
    )
}

/**
 * Panel del sistema. Una sola caja que agrupa filas separadas por una línea
 * de 1 dp, en vez de varias tarjetas flotando con su propio borde y su propia
 * sombra. Menos ruido y una jerarquía más clara.
 */
@Composable
fun WayraPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

/** Separador interno de [WayraPanel]. Sangrado para que la lista respire. */
@Composable
fun PanelDivider(startIndent: Int = 16) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startIndent.dp),
        thickness = 1.dp,
        color = BorderSoft
    )
}
