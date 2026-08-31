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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.repository.MonthProjection
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme

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
                ReportLoading()
            } else {
                incomeProjection?.let { projection ->
                    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                    val months = projection.projections

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                    ) {
                        item {
                            ReportHero(
                                caption = "Ingreso mensual esperado",
                                value = currency.format(projection.monthlyExpectedIncome),
                                note = "Según las suscripciones activas de hoy"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportStatStrip(
                                stats = listOf(
                                    ReportStat("Suscripciones", "${projection.totalActiveSubscriptions}"),
                                    ReportStat(
                                        "Por alumno",
                                        currency.format(projection.averageRevenuePerStudent)
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionLabel(text = "Próximos ${months.size} meses")
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (months.isEmpty()) {
                            item { ReportEmpty(text = "No hay meses para proyectar todavía.") }
                        } else {
                            itemsIndexed(months) { index, month ->
                                ReportRowItem(
                                    title = "${month.monthName} ${month.year}",
                                    subtitle = if (month.pendingPayments > 0)
                                        "${month.pendingPayments} " +
                                        (if (month.pendingPayments == 1) "pago vence" else "pagos vencen") +
                                        " · ${currency.format(month.pendingAmount)}"
                                    else "Sin vencimientos",
                                    iconRes = R.drawable.ic_calendar,
                                    iconTone = if (month.pendingPayments > 0) Tone.Warning else Tone.Neutral,
                                    amount = currency.format(month.expectedIncome),
                                    isFirst = index == 0,
                                    isLast = index == months.lastIndex
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Nota metodologica: una proyeccion sin sus
                            // supuestos a la vista invita a leerla como certeza.
                            WayraPanel(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        contentDescription = null,
                                        tint = InkSubtle,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "El estimado toma las suscripciones activas de hoy y asume " +
                                                "que no cambian. Los vencimientos son los pagos con fecha " +
                                                "en cada mes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkMuted
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

