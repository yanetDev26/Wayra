package com.app.wayra.ui.reports

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.repository.PlanStats
import com.app.wayra.ui.theme.Cream
import com.app.wayra.ui.theme.Gray
import com.app.wayra.ui.theme.WayraOrange
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanStatisticsReportScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val planStats by viewModel.planStatistics.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.loadPlanStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas por Plan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WayraOrange,
                    titleContentColor = Cream
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WayraOrange)
            }
        } else {
            planStats?.let { stats ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    // Header con totales generales
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = WayraOrange
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Resumen General",
                                    fontSize = 16.sp,
                                    color = Cream,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${stats.totalActiveStudents}",
                                    fontSize = 36.sp,
                                    color = Cream,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Alumnos Activos",
                                    fontSize = 14.sp,
                                    color = Cream.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Resumen de ingresos
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Ingresos del mes
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Cream
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                            .format(stats.totalMonthlyRevenue),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WayraOrange
                                    )
                                    Text(
                                        text = "Mes Actual",
                                        fontSize = 12.sp,
                                        color = Gray
                                    )
                                }
                            }

                            // Ingresos totales
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Cream
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                            .format(stats.totalRevenue),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WayraOrange
                                    )
                                    Text(
                                        text = "Total",
                                        fontSize = 12.sp,
                                        color = Gray
                                    )
                                }
                            }
                        }
                    }

                    // Título de planes
                    item {
                        Text(
                            text = "Detalle por Plan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lista de planes
                    items(stats.planStats) { planStat ->
                        PlanStatCard(planStat = planStat)
                    }

                    // Mensaje si no hay planes
                    if (stats.planStats.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Cream
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay planes con alumnos activos",
                                        color = Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanStatCard(planStat: PlanStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Cream
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = WayraOrange.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = planStat.planName.firstOrNull()?.toString()?.uppercase() ?: "P",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = WayraOrange
                        )
                    }
                    Column {
                        Text(
                            text = planStat.planName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Gray
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                .format(planStat.price) + " / mes",
                            fontSize = 12.sp,
                            color = Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas del plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Estudiantes activos
                StatItem(
                    modifier = Modifier.weight(1f),
                    label = "Alumnos",
                    value = "${planStat.activeStudents}",
                    icon = R.drawable.student
                )

                // Ingresos del mes
                StatItem(
                    modifier = Modifier.weight(1f),
                    label = "Mes Actual",
                    value = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                        .format(planStat.monthlyRevenue),
                    icon = R.drawable.calendar
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ingresos totales
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WayraOrange.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.money),
                            contentDescription = "Icono",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Ingresos Totales",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                            .format(planStat.totalRevenue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WayraOrange
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Int
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Cream
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Icono",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WayraOrange
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Gray
            )
        }
    }
}
