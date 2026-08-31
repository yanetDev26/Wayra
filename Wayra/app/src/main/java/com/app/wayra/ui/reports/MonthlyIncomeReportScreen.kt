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
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WayraOrange)
                }
            } else {
                monthlyStats?.let { stats ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                    ) {
                    // Header con mes actual
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
                                    text = getCurrentMonthName(),
                                    fontSize = 16.sp,
                                    color = OnBrand,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                        .format(stats.totalIncome),
                                    fontFamily = AgenorNeue,
                                    fontSize = 32.sp,
                                    color = OnBrand,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total Recaudado",
                                    fontSize = 14.sp,
                                    color = OnBrand.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Resumen
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total de pagos
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
                                        text = "${stats.totalPayments}",
                                        fontFamily = AgenorNeue,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Pagos",
                                        fontSize = 12.sp,
                                        color = Ink
                                    )
                                }
                            }

                            // Promedio
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
                                        text = if (stats.totalPayments > 0) {
                                            "$ ${String.format("%.0f", stats.totalIncome / stats.totalPayments)}"
                                        } else {
                                            "$ 0"
                                        },
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

                    // Por metodo de pago
                    item {
                        Text(
                            text = "Por Método de Pago",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(stats.paymentsByMethod.entries.toList()) { (method, data) ->
                        PaymentMethodCard(
                            method = method,
                            count = data.first,
                            total = data.second
                        )
                    }

                    // Detalle de pagos
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detalle de Pagos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(stats.payments) { payment ->
                        PaymentDetailCard(payment = payment)
                    }
                }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    count: Int,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = WayraOrangeSoft,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getPaymentMethodIcon(method)),
                        contentDescription = null,
                        tint = WayraOrangeDark,
                        modifier = Modifier.size(20.dp)
                    )

                }
                Column {
                    Text(
                        text = getPaymentMethodName(method),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = "$count ${if (count == 1) "pago" else "pagos"}",
                        fontSize = 12.sp,
                        color = Ink
                    )
                }
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(total),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Ink
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PaymentDetailCard(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
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
                    text = formatDate(payment.paymentDate ?: 0L),
                    fontSize = 12.sp,
                    color = Ink
                )
                Text(
                    text = getPaymentMethodName(payment.paymentMethod ?: PaymentMethod.EFECTIVO),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
            }
            Text(
                text = "$ ${String.format("%.2f", payment.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
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
