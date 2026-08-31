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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.data.model.Payment
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingPaymentsReportScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val pendingStats by viewModel.pendingPaymentsStats.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingPaymentsReport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos Pendientes") },
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
                pendingStats?.let { stats ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                    // Header con estadísticas principales
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card principal con total pendiente
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Danger
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
                                    text = "Total Pendiente",
                                    fontSize = 16.sp,
                                    color = OnDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                        .format(stats.totalPending),
                                    fontFamily = AgenorNeue,
                                    fontSize = 32.sp,
                                    color = OnDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${stats.uniqueStudents} alumnos",
                                    fontSize = 14.sp,
                                    color = OnDark.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Tarjetas de resumen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Vencidos
                            StatCard(
                                title = "Vencidos",
                                value = "${stats.overdueCount}",
                                subtitle = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                    .format(stats.totalOverdue),
                                color = Danger,
                                modifier = Modifier.weight(1f)
                            )

                            // Próximos 7 días
                            StatCard(
                                title = "Próximos",
                                value = "${stats.upcomingSoonCount}",
                                subtitle = "7 días",
                                color = WayraOrange,
                                modifier = Modifier.weight(1f)
                            )

                            // Total pendientes
                            StatCard(
                                title = "Total",
                                value = "${stats.pendingCount}",
                                subtitle = "pendientes",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Tabs para filtrar
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Todos (${stats.pendingCount})") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Vencidos (${stats.overdueCount})") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Próximos (${stats.upcomingSoonCount})") }
                        )
                    }

                    // Lista de pagos
                    val paymentsToShow = when (selectedTab) {
                        0 -> stats.allPendingPayments
                        1 -> stats.overduePayments
                        2 -> stats.upcomingPayments
                        else -> stats.allPendingPayments
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        )
                    ) {
                        items(paymentsToShow) { payment ->
                            PendingPaymentCard(payment = payment)
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = AgenorNeue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Ink
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Ink
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PendingPaymentCard(payment: Payment) {
    val isOverdue = (payment.dueDate ?: Long.MAX_VALUE) < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) {
                DangerSoft
            } else {
                Paper
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Vence: ${formatPendingDate(payment.dueDate ?: 0L)}",
                    fontSize = 12.sp,
                    color = if (isOverdue) Danger else Ink,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isOverdue) {
                    val daysOverdue = ((System.currentTimeMillis() - (payment.dueDate ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()
                    Text(
                        text = "Vencido hace $daysOverdue ${if (daysOverdue == 1) "día" else "días"}",
                        fontSize = 12.sp,
                        color = Danger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$ ${String.format("%.2f", payment.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) Danger else WayraOrange
                )
                if (payment.notes.isNotEmpty()) {
                    Text(
                        text = payment.notes,
                        fontSize = 12.sp,
                        color = Ink
                    )
                }
            }
        }
    }
}

private fun formatPendingDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp)
}
