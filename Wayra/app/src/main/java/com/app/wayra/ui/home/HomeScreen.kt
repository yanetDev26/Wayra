package com.app.wayra.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.components.WayraBackground
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToRegisterPayment: () -> Unit = {},
    onNavigateToActiveStudents: () -> Unit = {},
    onNavigateToNewStudents: () -> Unit = {},
    onNavigateToPendingThisMonth: () -> Unit = {},
    onNavigateToPendingLastMonth: () -> Unit = {}
) {
    val stats by viewModel.stats.observeAsState(HomeStats())
    val currentDate by viewModel.currentDate.observeAsState("")

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    HomeContent(
        stats = stats,
        currentDate = currentDate,
        onRegisterPaymentClick = onNavigateToRegisterPayment,
        onActiveStudentsClick = onNavigateToActiveStudents,
        onNewStudentsClick = onNavigateToNewStudents,
        onPendingThisMonthClick = onNavigateToPendingThisMonth,
        onPendingLastMonthClick = onNavigateToPendingLastMonth,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    stats: HomeStats,
    currentDate: String,
    onRegisterPaymentClick: () -> Unit,
    onActiveStudentsClick: () -> Unit = {},
    onNewStudentsClick: () -> Unit = {},
    onPendingThisMonthClick: () -> Unit = {},
    onPendingLastMonthClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAmount by remember { mutableStateOf(false) }

    WayraBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Inicio es la unica pantalla sin barra superior, asi que tiene
                // que reservar ella misma el alto de la barra de estado: si no,
                // el logo queda pegado al borde. Va antes del scroll para que
                // el margen no se desplace al deslizar.
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Encabezado alineado a la izquierda. Centrar todo es lo que hace
            // que una pantalla se lea como una plantilla.
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 22.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_wayra),
                    contentDescription = "Wayra Running Team",
                    modifier = Modifier.height(64.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                SectionLabel(text = currentDate)
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SectionLabel(text = "Resumen del mes")
                Spacer(modifier = Modifier.height(10.dp))

                // Las cuatro metricas van en un solo panel separado por lineas,
                // no en cuatro tarjetas flotantes: menos ruido y una sola caja
                // que leer.
                WayraPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            MetricCell(
                                label = stringResource(R.string.home_active_students),
                                value = stats.activeStudents.toString(),
                                iconRes = R.drawable.ic_stopwatch,
                                tone = Tone.Brand,
                                modifier = Modifier.weight(1f),
                                onClick = onActiveStudentsClick
                            )
                            CellDivider()
                            MetricCell(
                                label = "Nuevos este mes",
                                value = stats.newStudentsThisMonth.toString(),
                                iconRes = R.drawable.ic_user_plus,
                                tone = Tone.Success,
                                modifier = Modifier.weight(1f),
                                onClick = onNewStudentsClick
                            )
                        }
                        PanelDivider(startIndent = 0)
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            MetricCell(
                                label = "Pendientes este mes",
                                value = stats.pendingThisMonth.toString(),
                                iconRes = R.drawable.ic_clock,
                                tone = Tone.Warning,
                                modifier = Modifier.weight(1f),
                                onClick = onPendingThisMonthClick
                            )
                            CellDivider()
                            MetricCell(
                                label = "Vencidos mes pasado",
                                value = stats.pendingLastMonth.toString(),
                                iconRes = R.drawable.ic_overdue,
                                tone = Tone.Danger,
                                modifier = Modifier.weight(1f),
                                onClick = onPendingLastMonthClick
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionLabel(text = "Caja")
                Spacer(modifier = Modifier.height(10.dp))

                // Unico bloque solido de la pantalla: el foco visual es uno.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_wallet),
                                    contentDescription = null,
                                    tint = OnDark.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.home_total_collected).uppercase(),
                                    style = WayraType.sectionLabel,
                                    color = OnDark.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = if (showAmount) formatCurrency(stats.totalCollected) else "\u2022\u2022\u2022\u2022\u2022\u2022",
                                style = WayraType.metricHero,
                                color = OnDark,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    color = OnDark.copy(alpha = 0.10f),
                                    shape = CircleShape
                                )
                                .clickable { showAmount = !showAmount },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (showAmount) R.drawable.ic_eye else R.drawable.ic_eye_off
                                ),
                                contentDescription = if (showAmount) "Ocultar importe" else "Mostrar importe",
                                tint = OnDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Accion principal: el unico naranja solido de la pantalla.
                Button(
                    onClick = onRegisterPaymentClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WayraOrange,
                        contentColor = OnBrand
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_register_payment),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/** Separador vertical entre dos celdas de metrica. */
@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(BorderSoft)
    )
}

/**
 * Celda de metrica. El color vive en el icono y en la cifra; el fondo se
 * mantiene neutro. Cuatro rectangulos pastel compiten entre si y no dejan leer
 * cual importa.
 */
@Composable
private fun MetricCell(
    label: String,
    value: String,
    iconRes: Int,
    tone: Tone,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val accent = when (tone) {
        Tone.Success -> Success
        Tone.Warning -> Warning
        Tone.Danger -> Danger
        Tone.Brand -> WayraOrangeDark
        Tone.Neutral -> InkMuted
    }
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            style = WayraType.metric,
            color = if (value == "0") InkSubtle else accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
            maxLines = 2
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    return format.format(amount)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeContent(
            stats = HomeStats(
                activeStudents = 24,
                newStudentsThisMonth = 4,
                totalCollected = 45000.0,
                pendingThisMonth = 5,
                pendingLastMonth = 3
            ),
            currentDate = "25 de Diciembre, 2025",
            onRegisterPaymentClick = {},
        )
    }
}
