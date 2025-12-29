package com.app.wayra.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToStudents: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    onNavigateToRegisterPayment: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAssignPlan: () -> Unit = {}
) {
    val stats by viewModel.stats.observeAsState(HomeStats())
    val currentDate by viewModel.currentDate.observeAsState("")
    val upcomingPayments by viewModel.upcomingPayments.observeAsState(emptyList())

    HomeContent(
        stats = stats,
        currentDate = currentDate,
        upcomingPayments = upcomingPayments,
        onAddStudentClick = onNavigateToStudents,
        onRegisterPaymentClick = onNavigateToRegisterPayment,
        onViewReportsClick = onNavigateToReports,
        onNavigateToStudents = onNavigateToStudents,
        onNavigateToPlans = onNavigateToPlans,
        onNavigateToAssignPlan = onNavigateToAssignPlan,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    stats: HomeStats,
    currentDate: String,
    upcomingPayments: List<Payment>,
    onAddStudentClick: () -> Unit,
    onRegisterPaymentClick: () -> Unit,
    onViewReportsClick: () -> Unit,
    onNavigateToStudents: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    onNavigateToAssignPlan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.home_welcome),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = currentDate,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Section Title
        Text(
            text = stringResource(R.string.home_stats_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Cards
        StatCard(
            title = stringResource(R.string.home_active_students),
            value = stats.activeStudents.toString(),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = stringResource(R.string.home_pending_payments),
            value = stats.pendingPayments.toString(),
            color = Color(0xFFFF9800)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = stringResource(R.string.home_total_collected),
            value = formatCurrency(stats.totalCollected),
            color = Color(0xFF4CAF50),
            valueSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Section
        Text(
            text = stringResource(R.string.home_quick_actions),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddStudentClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_add_student),
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }

            OutlinedButton(
                onClick = onRegisterPaymentClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_register_payment),
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }

            OutlinedButton(
                onClick = onViewReportsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_view_reports),
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.students_title),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onNavigateToPlans,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.plans_title),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onNavigateToAssignPlan,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_assign_plan),
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Payments Section
        Text(
            text = stringResource(R.string.home_upcoming_payments),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (upcomingPayments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.home_no_payments),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            upcomingPayments.forEach { payment ->
                PaymentItem(payment = payment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    valueSize: TextUnit = 32.sp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = valueSize,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentItem(
    payment: Payment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alumno ${payment.studentId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Plan",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                payment.dueDate?.let { dueDate ->
                    Text(
                        text = formatDueDate(dueDate),
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Text(
                text = formatCurrency(payment.amount),
                fontSize = 18.sp,
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
                pendingPayments = 8,
                totalCollected = 45000.0
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
            onAddStudentClick = {},
            onRegisterPaymentClick = {},
            onViewReportsClick = {},
            onNavigateToStudents = {},
            onNavigateToPlans = {},
            onNavigateToAssignPlan = {}
        )
    }
}
