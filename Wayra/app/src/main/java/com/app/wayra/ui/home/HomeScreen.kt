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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToRegisterPayment: () -> Unit = {}
) {
    val stats by viewModel.stats.observeAsState(HomeStats())
    val currentDate by viewModel.currentDate.observeAsState("")

    // Refrescar datos cuando la pantalla vuelve a estar visible
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    HomeContent(
        stats = stats,
        currentDate = currentDate,
        onRegisterPaymentClick = onNavigateToRegisterPayment,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    stats: HomeStats,
    currentDate: String,
    onRegisterPaymentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAmount by remember { mutableStateOf(false) }

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
                            text = if (showAmount) formatCurrency(stats.totalCollected) else "••••••",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Botón de ojo para mostrar/ocultar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable { showAmount = !showAmount },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showAmount) "👁️" else "🙈",
                            fontSize = 20.sp
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
            onRegisterPaymentClick = {}
        )
    }
}
