package com.app.wayra.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentStatus
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToRegisterPayment: () -> Unit = {},
    onNavigateToPaymentDetail: (String) -> Unit = {}
) {
    val stats by viewModel.stats.observeAsState(HomeStats())
    val currentDate by viewModel.currentDate.observeAsState("")
    val upcomingPayments by viewModel.upcomingPayments.observeAsState(emptyList())

    HomeContent(
        stats = stats,
        currentDate = currentDate,
        upcomingPayments = upcomingPayments,
        onRegisterPaymentClick = onNavigateToRegisterPayment,
        onPaymentClick = onNavigateToPaymentDetail,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    stats: HomeStats,
    currentDate: String,
    upcomingPayments: List<Payment>,
    onRegisterPaymentClick: () -> Unit,
    onPaymentClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Header con logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2C2C2C), // Gris oscuro
                            Color(0xFF1A1A1A), // Negro medio
                            Color(0xFF000000)  // Negro puro
                        )
                    )
                )
                .padding(vertical = 24.dp, horizontal = 16.dp),
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
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = currentDate,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Contenido con padding
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Statistics Cards en grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCardCompact(
                    title = stringResource(R.string.home_active_students),
                    value = stats.activeStudents.toString(),
                    emoji = "👥",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                StatCardCompact(
                    title = "Nuevos este mes",
                    value = stats.newStudentsThisMonth.toString(),
                    emoji = "✨",
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
                    title = "Faltan abonar este mes",
                    value = stats.pendingThisMonth.toString(),
                    emoji = "📅",
                    backgroundColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )

                StatCardCompact(
                    title = "Faltan abonar mes pasado",
                    value = stats.pendingLastMonth.toString(),
                    emoji = "🔴",
                    backgroundColor = Color(0xFFFFCDD2),
                    contentColor = Color(0xFFB71C1C),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total collected - card destacada
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
                            Text(
                                text = "💰",
                                fontSize = 24.sp
                            )
                            Text(
                                text = stringResource(R.string.home_total_collected),
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = formatCurrency(stats.totalCollected),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Actions Section
            Text(
                text = stringResource(R.string.home_quick_actions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(28.dp))

            // Upcoming Payments Section
            Text(
                text = stringResource(R.string.home_upcoming_payments),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (upcomingPayments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 40.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_no_payments),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                upcomingPayments.forEach { payment ->
                    PaymentItemModern(
                        payment = payment,
                        onClick = { onPaymentClick(payment.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCardCompact(
    title: String,
    value: String,
    emoji: String,
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
            Text(
                text = emoji,
                fontSize = 28.sp
            )
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

@Composable
fun PaymentItemModern(
    payment: Payment,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Indicador de estado visual
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Color(0xFFFF9800),
                            shape = CircleShape
                        )
                )

                Column {
                    Text(
                        text = "Alumno ${payment.studentId}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    payment.dueDate?.let { dueDate ->
                        Text(
                            text = formatDueDate(dueDate),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = formatCurrency(payment.amount),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    return format.format(amount)
}

private fun formatDueDate(dueDate: Long): String {
    val date = Date(dueDate)
    val now = Calendar.getInstance()
    val dueCalendar = Calendar.getInstance().apply { time = date }

    val diffInDays = ((dueCalendar.timeInMillis - now.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    return when {
        diffInDays == 0 -> "Vence hoy"
        diffInDays == 1 -> "Vence mañana"
        diffInDays > 1 -> "Vence en $diffInDays días"
        else -> "Vencido"
    }
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
            upcomingPayments = listOf(
                Payment(
                    id = "1",
                    studentId = "Juan Pérez",
                    subscriptionId = "sub1",
                    amount = 5000.0,
                    dueDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000),
                    status = PaymentStatus.PENDIENTE
                )
            ),
            onRegisterPaymentClick = {}
        )
    }
}
