package com.app.wayra.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.components.WayraBackground
import com.app.wayra.ui.theme.Cream
import com.app.wayra.ui.theme.Gray
import com.app.wayra.ui.theme.Orange
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToRegisterPayment: () -> Unit = {},
    onNavigateToPendingPayments: (String) -> Unit = {}
) {
    val stats by viewModel.stats.observeAsState(HomeStats())
    val currentDate by viewModel.currentDate.observeAsState("")

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    HomeContent(
        stats = stats,
        currentDate = currentDate,
        onRegisterPaymentClick = onNavigateToRegisterPayment,
        onPendingPaymentsClick = onNavigateToPendingPayments,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    stats: HomeStats,
    currentDate: String,
    onRegisterPaymentClick: () -> Unit,
    onPendingPaymentsClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAmount by remember { mutableStateOf(false) }

    WayraBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Header con logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo del gimnasio
                    Image(
                        painter = painterResource(id = R.drawable.logo_wayra),
                        contentDescription = "Logo Wayra",
                        modifier = Modifier.size(130.dp)
                    )

                    Text(
                        text = currentDate,
                        fontSize = 14.sp,
                        color = Gray
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCardCompact(
                        title = stringResource(R.string.home_active_students),
                        value = stats.activeStudents.toString(),
                        iconRes = R.drawable.gym_active,
                        backgroundColor = Orange,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    StatCardCompact(
                        title = "Nuevos este mes",
                        value = stats.newStudentsThisMonth.toString(),
                        iconRes = R.drawable.gym_new_active,
                        backgroundColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjetas de deudores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCardCompact(
                        title = "Pendientes abonar este mes",
                        value = stats.pendingThisMonth.toString(),
                        iconRes = R.drawable.gym_pay,
                        backgroundColor = Color(0xFFFFEBEE),
                        contentColor = Color(0xFFC62828),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPendingPaymentsClick("this_month") }
                    )

                    StatCardCompact(
                        title = "Pendientes abonar mes pasado",
                        value = stats.pendingLastMonth.toString(),
                        iconRes = R.drawable.gym_pay_past_month,
                        backgroundColor = Color(0xFFFFCDD2),
                        contentColor = Color(0xFFB71C1C),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPendingPaymentsClick("last_month") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.payment),
                                    contentDescription = "Icono de pago",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )

                                Text(
                                    text = stringResource(R.string.home_total_collected),
                                    fontSize = 14.sp,
                                    color = Cream,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (showAmount) formatCurrency(stats.totalCollected) else "••••••",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cream,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { showAmount = !showAmount },
                            contentAlignment = Alignment.Center
                        ) {
                            if (showAmount) {
                                Icon(
                                    painter = painterResource(id = R.drawable.eye_on),
                                    contentDescription = "Icono de ojo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.eye_off),
                                    contentDescription = "Icono de ojo cerrado",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Acción principal: Registrar pago
                FilledTonalButton(
                    onClick = onRegisterPaymentClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_register_payment),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun StatCardCompact(
    title: String,
    value: String,
    iconRes: Int? = null,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            when {
                iconRes != null -> {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    return format.format(amount)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeContent(
            stats = HomeStats(
                activeStudents = 24,
                newStudentsThisMonth = 4,
                totalCollected = 45000.0,
                pendingThisMonth = 5,
                pendingLastMonth = 3
            ),
            currentDate = "25 de Diciembre, 2025",
            onRegisterPaymentClick = {},
            onPendingPaymentsClick = {}
        )
    }
}
