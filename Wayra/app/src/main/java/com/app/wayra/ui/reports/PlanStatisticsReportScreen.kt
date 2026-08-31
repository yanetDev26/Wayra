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
import com.app.wayra.data.repository.PlanStats
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed
import kotlin.math.roundToInt

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
                planStats?.let { stats ->
                    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                    val plans = stats.planStats
                    val totalStudents = stats.totalActiveStudents.coerceAtLeast(1)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                    ) {
                        item {
                            ReportHero(
                                caption = "Alumnos activos",
                                value = "${stats.totalActiveStudents}",
                                note = "${currency.format(stats.totalMonthlyRevenue)} por mes"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportStatStrip(
                                stats = listOf(
                                    ReportStat("Planes con alumnos", "${plans.size}"),
                                    ReportStat(
                                        "Ingreso por alumno",
                                        if (stats.totalActiveStudents > 0)
                                            currency.format(stats.totalMonthlyRevenue / stats.totalActiveStudents)
                                        else currency.format(0)
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionLabel(text = "Reparto de alumnos")
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (plans.isEmpty()) {
                            item { ReportEmpty(text = "Todavía no hay planes con alumnos activos.") }
                        } else {
                            itemsIndexed(plans) { index, planStat ->
                                val share = planStat.activeStudents.toFloat() / totalStudents
                                ReportShareRow(
                                    title = planStat.planName,
                                    subtitle = "${planStat.activeStudents} " +
                                            (if (planStat.activeStudents == 1) "alumno" else "alumnos") +
                                            " · ${(share * 100).roundToInt()}%",
                                    amount = currency.format(planStat.monthlyRevenue),
                                    fraction = share,
                                    isFirst = index == 0,
                                    isLast = index == plans.lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

