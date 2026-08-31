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
import androidx.compose.foundation.lazy.itemsIndexed
import com.app.wayra.R

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
                ReportLoading()
            } else {
                pendingStats?.let { stats ->
                    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                    val paymentsToShow = when (selectedTab) {
                        1 -> stats.overduePayments
                        2 -> stats.upcomingPayments
                        else -> stats.allPendingPayments
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                    ) {
                        item {
                            // El bloque de cabecera va en slate, no en rojo a
                            // sangre: la severidad la llevan las metricas y las
                            // filas, donde ademas distingue vencido de proximo.
                            ReportHero(
                                caption = "Total pendiente",
                                value = currency.format(stats.totalPending),
                                note = "${stats.uniqueStudents} ${if (stats.uniqueStudents == 1) "alumno" else "alumnos"}"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportStatStrip(
                                stats = listOf(
                                    ReportStat("Vencidos", "${stats.overdueCount}", Tone.Danger),
                                    ReportStat("Próximos 7 días", "${stats.upcomingSoonCount}", Tone.Warning),
                                    ReportStat("Total", "${stats.pendingCount}", Tone.Neutral)
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ReportSegmented(
                                options = listOf(
                                    "Todos (${stats.pendingCount})",
                                    "Vencidos (${stats.overdueCount})",
                                    "Próximos (${stats.upcomingSoonCount})"
                                ),
                                selectedIndex = selectedTab,
                                onSelect = { selectedTab = it }
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (paymentsToShow.isEmpty()) {
                            item {
                                ReportEmpty(
                                    text = when (selectedTab) {
                                        1 -> "No hay pagos vencidos. Al día."
                                        2 -> "Nada vence en los próximos 7 días."
                                        else -> "No hay pagos pendientes."
                                    },
                                    iconRes = R.drawable.ic_check
                                )
                            }
                        } else {
                            itemsIndexed(paymentsToShow) { index, payment ->
                                val due = payment.dueDate ?: Long.MAX_VALUE
                                val isOverdue = due < System.currentTimeMillis()
                                val days = ((System.currentTimeMillis() - (payment.dueDate ?: 0L)) /
                                        (1000 * 60 * 60 * 24)).toInt()
                                ReportRowItem(
                                    title = if (payment.notes.isNotEmpty()) payment.notes
                                            else "Vence ${formatPendingDate(payment.dueDate ?: 0L)}",
                                    subtitle = if (payment.notes.isNotEmpty())
                                        "Vence ${formatPendingDate(payment.dueDate ?: 0L)}" else null,
                                    iconRes = if (isOverdue) R.drawable.ic_overdue else R.drawable.ic_clock,
                                    iconTone = if (isOverdue) Tone.Danger else Tone.Warning,
                                    amount = currency.format(payment.amount),
                                    amountTone = if (isOverdue) Tone.Danger else Tone.Neutral,
                                    badgeText = if (isOverdue)
                                        "$days ${if (days == 1) "día" else "días"}" else null,
                                    badgeTone = Tone.Danger,
                                    isFirst = index == 0,
                                    isLast = index == paymentsToShow.lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPendingDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp)
}
