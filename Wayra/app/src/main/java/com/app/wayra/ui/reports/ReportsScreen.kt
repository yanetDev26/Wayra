package com.app.wayra.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reportes Financieros",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                ReportCard(
                    title = "Ingresos del Mes",
                    description = "Total recaudado en el mes actual"
                )
            }

            item {
                ReportCard(
                    title = "Pagos Pendientes",
                    description = "Estudiantes con pagos atrasados"
                )
            }

            item {
                ReportCard(
                    title = "Estadísticas por Plan",
                    description = "Distribución de estudiantes por plan"
                )
            }

            item {
                ReportCard(
                    title = "Historial de Pagos",
                    description = "Consulta pagos por fecha o estudiante"
                )
            }

            item {
                ReportCard(
                    title = "Proyección de Ingresos",
                    description = "Estimado de ingresos próximos meses"
                )
            }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    description: String
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
                onClick = { /* TODO: Implementar navegación al reporte */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Reporte")
            }
        }
    }
}
