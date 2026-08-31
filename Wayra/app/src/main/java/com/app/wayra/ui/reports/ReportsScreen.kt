package com.app.wayra.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                title = { Text("Reportes Financieros") },
                colors = wayraTopAppBarColors()
            )
        },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            WayraBackground(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                ) {
                item {
                    ReportCard(
                        title = "Ingresos del Mes",
                        description = "Total recaudado en el mes actual",
                        onClick = onNavigateToMonthlyIncome
                    )
                }

                item {
                    ReportCard(
                        title = "Pagos Pendientes",
                        description = "Alumnos con pagos atrasados",
                        onClick = onNavigateToPendingPayments
                    )
                }

                item {
                    ReportCard(
                        title = "Estadísticas por Plan",
                        description = "Distribución de alumnos por plan",
                        onClick = onNavigateToPlanStatistics
                    )
                }

                item {
                    ReportCard(
                        title = "Historial de Pagos",
                        description = "Consulta pagos por fecha o alumno",
                        onClick = onNavigateToPaymentHistory
                    )
                }

                item {
                    ReportCard(
                        title = "Proyección de Ingresos",
                        description = "Estimado de ingresos próximos meses",
                        onClick = onNavigateToIncomeProjection
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Reporte")
            }
        }
    }
}
