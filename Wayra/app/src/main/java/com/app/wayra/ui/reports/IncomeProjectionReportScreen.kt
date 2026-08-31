package com.app.wayra.ui.reports

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.app.wayra.data.repository.MonthProjection
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeProjectionReportScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val incomeProjection by viewModel.incomeProjection.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.loadIncomeProjection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proyección de Ingresos") },
                colors = wayraTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WayraOrange)
                }
            } else {
                incomeProjection?.let { projection ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                    ) {
                    // Header con ingreso mensual esperado
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = WayraOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Ingreso Mensual Esperado",
                                    fontSize = 16.sp,
                                    color = OnBrand,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                        .format(projection.monthlyExpectedIncome),
                                    fontFamily = AgenorNeue,
                                    fontSize = 32.sp,
                                    color = OnBrand,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Basado en suscripciones activas",
                                    fontSize = 14.sp,
                                    color = OnBrand.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Estadísticas generales
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Estudiantes activos
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSoft),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceCard
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${projection.totalActiveSubscriptions}",
                                        fontFamily = AgenorNeue,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Alumnos",
                                        fontSize = 12.sp,
                                        color = Ink
                                    )
                                }
                            }

                            // Promedio por estudiante
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSoft),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceCard
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
                                            .format(projection.averageRevenuePerStudent),
                                        fontFamily = AgenorNeue,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Promedio",
                                        fontSize = 12.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }

                    // Título de proyecciones
                    item {
                        Text(
                            text = "Próximos ${projection.projections.size} meses",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lista de proyecciones mensuales
                    items(projection.projections) { monthProjection ->
                        MonthProjectionCard(monthProjection = monthProjection)
                    }

                    // Información adicional
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSoft),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceCard
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = null,
                                    tint = Warning,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "• El ingreso esperado se calcula basándose en las suscripciones activas actuales\n" +
                                            "• Los pagos pendientes son los que tienen vencimiento en cada mes\n" +
                                            "• Esta proyección asume que no habrá cambios en las suscripciones",
                                    fontSize = 12.sp,
                                    color = Ink,
                                    lineHeight = 18.sp
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
fun MonthProjectionCard(monthProjection: MonthProjection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del mes
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
                                color = WayraOrangeSoft,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthProjection.monthName.take(3),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }
                    Column {
                        Text(
                            text = monthProjection.monthName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                        Text(
                            text = "${monthProjection.year}",
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }

                // Ingreso esperado
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                            .format(monthProjection.expectedIncome),
                        fontFamily = AgenorNeue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = "Esperado",
                        fontSize = 12.sp,
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de pagos pendientes
            if (monthProjection.pendingPayments > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningSoft // Naranja claro
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${monthProjection.pendingPayments} ${if (monthProjection.pendingPayments == 1) "pago pendiente" else "pagos pendientes"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Warning
                            )
                            Text(
                                text = "Vencen en este mes",
                                fontSize = 12.sp,
                                color = Warning.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                .format(monthProjection.pendingAmount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Warning
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderSoft),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceCard
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin pagos pendientes registrados",
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }
            }
        }
    }
}
