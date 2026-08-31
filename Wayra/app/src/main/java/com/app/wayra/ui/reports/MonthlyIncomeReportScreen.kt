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
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyIncomeReportScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val monthlyStats by viewModel.monthlyIncomeStats.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.loadMonthlyIncomeReport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresos del Mes") },
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
                monthlyStats?.let { stats ->
                    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                    val methods = stats.paymentsByMethod.entries.toList()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                    ) {
                        item {
                            ReportHero(
                                caption = getCurrentMonthName(),
                                value = currency.format(stats.totalIncome),
                                note = "Total recaudado en el mes"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportStatStrip(
                                stats = listOf(
                                    ReportStat(
                                        label = if (stats.totalPayments == 1) "Pago" else "Pagos",
                                        value = "${stats.totalPayments}"
                                    ),
                                    ReportStat(
                                        label = "Promedio",
                                        value = if (stats.totalPayments > 0)
                                            currency.format(stats.totalIncome / stats.totalPayments)
                                        else currency.format(0)
                                    )
                                )
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionLabel(text = "Por método de pago")
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (methods.isEmpty()) {
                            item { ReportEmpty(text = "Todavía no hay pagos registrados este mes.") }
                        } else {
                            itemsIndexed(methods) { index, entry ->
                                val (method, data) = entry
                                ReportRowItem(
                                    title = getPaymentMethodName(method),
                                    subtitle = "${data.first} ${if (data.first == 1) "pago" else "pagos"}",
                                    iconRes = getPaymentMethodIcon(method),
                                    iconTone = Tone.Brand,
                                    amount = currency.format(data.second),
                                    isFirst = index == 0,
                                    isLast = index == methods.lastIndex
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionLabel(text = "Detalle de pagos")
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (stats.payments.isEmpty()) {
                            item { ReportEmpty(text = "Sin movimientos en el período.") }
                        } else {
                            itemsIndexed(stats.payments) { index, payment ->
                                ReportRowItem(
                                    title = getPaymentMethodName(
                                        payment.paymentMethod ?: PaymentMethod.EFECTIVO
                                    ),
                                    subtitle = formatDate(payment.paymentDate ?: 0L),
                                    amount = currency.format(payment.amount),
                                    amountTone = Tone.Success,
                                    isFirst = index == 0,
                                    isLast = index == stats.payments.lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getCurrentMonthName(): String {
    val calendar = Calendar.getInstance()
    val monthNames = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    return "${monthNames[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
}

fun getPaymentMethodName(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.EFECTIVO -> "Efectivo"
        PaymentMethod.TRANSFERENCIA -> "Transferencia"
    }
}

fun getPaymentMethodIcon(method: PaymentMethod): Int {
    return when (method) {
        PaymentMethod.EFECTIVO -> R.drawable.ic_money
        PaymentMethod.TRANSFERENCIA -> R.drawable.ic_transfer
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp)
}
