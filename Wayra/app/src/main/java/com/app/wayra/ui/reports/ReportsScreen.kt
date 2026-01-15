package com.app.wayra.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                title = { Text("Reportes Financieros") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
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
                    description = "Estudiantes con pagos atrasados",
                    onClick = onNavigateToPendingPayments
                )
            }

            item {
                ReportCard(
                    title = "Estadísticas por Plan",
                    description = "Distribución de estudiantes por plan",
                    onClick = onNavigateToPlanStatistics
                )
            }

            item {
                ReportCard(
                    title = "Historial de Pagos",
                    description = "Consulta pagos por fecha o estudiante",
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

@Composable
private fun ReportCard(
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
