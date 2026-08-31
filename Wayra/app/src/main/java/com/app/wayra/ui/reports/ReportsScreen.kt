package com.app.wayra.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.components.WayraBackground
import com.app.wayra.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToMonthlyIncome: () -> Unit = {},
    onNavigateToPendingPayments: () -> Unit = {},
    onNavigateToPlanStatistics: () -> Unit = {},
    onNavigateToPaymentHistory: () -> Unit = {},
    onNavigateToIncomeProjection: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                // "Reportes Financieros" repetia la pestaña que ya dice
                // "Reportes"; el titulo solo tiene que ubicar, no describir.
                title = { Text("Reportes") },
                colors = wayraTopAppBarColors()
            )
        },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        WayraBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
            ) {
                // Los reportes se agrupan por el periodo que miran. El orden
                // no es decorativo: primero lo que se puede accionar hoy.
                item {
                    SectionLabel(text = "Este mes")
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    ReportGroup(
                        items = listOf(
                            ReportEntry(
                                title = "Ingresos del mes",
                                description = "Total recaudado y desglose por medio de pago",
                                iconRes = R.drawable.ic_wallet,
                                tone = Tone.Brand,
                                onClick = onNavigateToMonthlyIncome
                            ),
                            ReportEntry(
                                title = "Pagos pendientes",
                                description = "Quién debe y desde cuándo",
                                iconRes = R.drawable.ic_clock,
                                tone = Tone.Warning,
                                onClick = onNavigateToPendingPayments
                            ),
                            ReportEntry(
                                title = "Estadísticas por plan",
                                description = "Cómo se reparten los alumnos entre planes",
                                iconRes = R.drawable.ic_pie,
                                tone = Tone.Neutral,
                                onClick = onNavigateToPlanStatistics
                            )
                        )
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionLabel(text = "Histórico y proyección")
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    ReportGroup(
                        items = listOf(
                            ReportEntry(
                                title = "Historial de pagos",
                                description = "Consulta por fecha, alumno o estado",
                                iconRes = R.drawable.ic_calendar,
                                tone = Tone.Neutral,
                                onClick = onNavigateToPaymentHistory
                            ),
                            ReportEntry(
                                title = "Proyección de ingresos",
                                description = "Estimado de los próximos meses",
                                iconRes = R.drawable.ic_report,
                                tone = Tone.Success,
                                onClick = onNavigateToIncomeProjection
                            )
                        )
                    )
                }
            }
        }
    }
}

private data class ReportEntry(
    val title: String,
    val description: String,
    val iconRes: Int,
    val tone: Tone,
    val onClick: () -> Unit
)

/** Grupo de reportes: un panel, filas separadas por linea sangrada. */
@Composable
private fun ReportGroup(items: List<ReportEntry>) {
    WayraPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            items.forEachIndexed { index, entry ->
                if (index > 0) PanelDivider(startIndent = 58)
                ReportRow(entry)
            }
        }
    }
}

@Composable
private fun ReportRow(entry: ReportEntry) {
    val accent = when (entry.tone) {
        Tone.Success -> Success
        Tone.Warning -> Warning
        Tone.Danger -> Danger
        Tone.Brand -> WayraOrangeDark
        Tone.Neutral -> InkMuted
    }
    val accentBg = when (entry.tone) {
        Tone.Success -> SuccessSoft
        Tone.Warning -> WarningSoft
        Tone.Danger -> DangerSoft
        Tone.Brand -> WayraOrangeSoft
        Tone.Neutral -> SurfaceAlt
    }
    // La fila entera es el area tactil. El boton "Ver Reporte" repetido cinco
    // veces era un ancho completo de naranja por tarjeta, sin agregar nada.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = entry.onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accentBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = entry.iconRes),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 15.sp,
                color = Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                maxLines = 2
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron),
            contentDescription = null,
            tint = InkSubtle,
            modifier = Modifier.size(15.dp)
        )
    }
}
