package com.app.wayra.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.theme.*

/**
 * Piezas compartidas por las cinco pantallas de reporte.
 *
 * Antes cada una repetía la misma estructura con su propia variante: una
 * tarjeta naranja a sangre con la cifra principal, dos tarjetas sueltas
 * debajo y títulos de sección en negrita de 18 sp. Al estar duplicada, la
 * estructura se había desincronizado entre pantallas.
 */

// ---------------------------------------------------------------------------
// Cifra principal
// ---------------------------------------------------------------------------

/**
 * Bloque de cabecera de un reporte.
 *
 * Va en slate, no en naranja. El naranja está reservado para la acción
 * principal, y el resumen de un reporte es un dato, no una acción: cinco
 * pantallas abriendo con un bloque naranja a sangre gastaban el acento.
 * Es el mismo bloque que usa la caja en Inicio, así que la app dice
 * "esta es la cifra que importa" siempre de la misma manera.
 */
@Composable
fun ReportHero(
    caption: String,
    value: String,
    note: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Text(
            text = caption.uppercase(),
            style = WayraType.sectionLabel,
            color = OnDark.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = value,
            style = WayraType.metricHero,
            color = OnDark
        )
        if (note != null) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = OnDark.copy(alpha = 0.65f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tira de métricas
// ---------------------------------------------------------------------------

data class ReportStat(
    val label: String,
    val value: String,
    val tone: Tone = Tone.Neutral
)

/**
 * Dos o tres métricas en un solo panel separado por líneas verticales, en vez
 * de tarjetas sueltas con su propio borde. Alineadas a la izquierda: centrar
 * cifras de distinto largo hace bailar la columna.
 */
@Composable
fun ReportStatStrip(
    stats: List<ReportStat>,
    modifier: Modifier = Modifier
) {
    WayraPanel(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BorderSoft)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stat.value,
                        style = WayraType.metric,
                        fontSize = 22.sp,
                        color = stat.tone.accent(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun Tone.accent(): Color = when (this) {
    Tone.Success -> Success
    Tone.Warning -> Warning
    Tone.Danger -> Danger
    Tone.Brand -> WayraOrangeDark
    Tone.Neutral -> Ink
}

@Composable
fun Tone.soft(): Color = when (this) {
    Tone.Success -> SuccessSoft
    Tone.Warning -> WarningSoft
    Tone.Danger -> DangerSoft
    Tone.Brand -> WayraOrangeSoft
    Tone.Neutral -> SurfaceAlt
}

// ---------------------------------------------------------------------------
// Filas de lista
// ---------------------------------------------------------------------------

/**
 * Fila de reporte. Cubre las listas de las cinco pantallas: método de pago,
 * detalle, pendientes, proyección por mes e historial.
 *
 * `isFirst` / `isLast` redondean solo los extremos, de modo que la lista se
 * lee como un panel y no como una pila de tarjetas.
 */
@Composable
fun ReportRowItem(
    title: String,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconRes: Int? = null,
    iconTone: Tone = Tone.Neutral,
    amount: String? = null,
    amountTone: Tone = Tone.Neutral,
    badgeText: String? = null,
    badgeTone: Tone = Tone.Neutral
) {
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 12.dp else 0.dp,
        topEnd = if (isFirst) 12.dp else 0.dp,
        bottomStart = if (isLast) 12.dp else 0.dp,
        bottomEnd = if (isLast) 12.dp else 0.dp
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, shape)
    ) {
        if (!isFirst) {
            HorizontalDivider(
                modifier = Modifier.padding(start = if (iconRes != null) 56.dp else 14.dp),
                thickness = 1.dp,
                color = BorderSoft
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(iconTone.soft(), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconTone.accent(),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (amount != null || badgeText != null) {
                Column(horizontalAlignment = Alignment.End) {
                    if (amount != null) {
                        Text(
                            text = amount,
                            style = WayraType.amount,
                            color = amountTone.accent()
                        )
                    }
                    if (badgeText != null) {
                        if (amount != null) Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(text = badgeText, tone = badgeTone)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Estados
// ---------------------------------------------------------------------------

@Composable
fun ReportLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = WayraOrange, strokeWidth = 2.5.dp)
    }
}

/** Estado vacío de una sección de reporte. Vive dentro del panel. */
@Composable
fun ReportEmpty(
    text: String,
    iconRes: Int = R.drawable.ic_info,
    modifier: Modifier = Modifier
) {
    WayraPanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = InkSubtle,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Filtro segmentado
// ---------------------------------------------------------------------------

/**
 * Filtro de tres opciones. Reemplaza al `TabRow` de Material, que traía sus
 * propios colores y su subrayado y no se parecía a nada más de la app.
 */
@Composable
fun ReportSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceAlt, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) SurfaceCard else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Ink else InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Barra de proporción
// ---------------------------------------------------------------------------

/**
 * Fila con barra de participación: nombre, valor y la porción que representa
 * sobre el total.
 *
 * Es una sola serie —cuánto pesa cada plan—, así que todas las barras usan el
 * mismo color: pintar cada fila de un color distinto codificaría el orden, que
 * ya lo da la posición, y encima gastaría la paleta de estados. El porcentaje
 * va escrito además de dibujado, de modo que la barra es refuerzo y no el
 * único canal de lectura.
 */
@Composable
fun ReportShareRow(
    title: String,
    subtitle: String,
    amount: String,
    fraction: Float,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 12.dp else 0.dp,
        topEnd = if (isFirst) 12.dp else 0.dp,
        bottomStart = if (isLast) 12.dp else 0.dp,
        bottomEnd = if (isLast) 12.dp else 0.dp
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, shape)
    ) {
        if (!isFirst) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 14.dp),
                thickness = 1.dp,
                color = BorderSoft
            )
        }
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
                Text(
                    text = amount,
                    style = WayraType.amount,
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Riel completo + relleno proporcional, ambos con extremos
            // redondeados y anclados al inicio.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(DataTrack, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(5.dp)
                        .background(DataAccent, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
