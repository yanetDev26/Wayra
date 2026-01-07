package com.app.wayra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.app.wayra.navigation.StudentsNavHost
import com.app.wayra.ui.home.HomeScreen
import com.app.wayra.ui.home.HomeViewModel
import com.app.wayra.ui.plans.PlansScreen
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.reports.ReportsScreen
import com.app.wayra.ui.students.StudentsViewModel
import com.app.wayra.ui.theme.WayraOrange
import com.app.wayra.ui.theme.WayraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen antes de super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            WayraTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Inicio", "Alumnos", "Planes", "Reportes")

    Scaffold(
        bottomBar = {
            ModernBottomBar(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> {
                // Tab Inicio: Dashboard con métricas y acciones rápidas
                val homeViewModel: HomeViewModel = viewModel()
                rememberNavController()
                HomeScreen(
                    viewModel = homeViewModel,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToStudents = { selectedItem = 1 },
                    onNavigateToPlans = { selectedItem = 2 },
                    onNavigateToRegisterPayment = {
                        // Navegar internamente dentro de Home
                    },
                    onNavigateToReports = { selectedItem = 3 },
                    onNavigateToAssignPlan = {
                        // Navegar internamente dentro de Home
                    }
                )
            }
            1 -> {
                // Tab Alumnos: ABM completo de estudiantes
                val studentsViewModel: StudentsViewModel = viewModel()
                val studentsNavController = rememberNavController()
                StudentsNavHost(
                    navController = studentsNavController,
                    studentsViewModel = studentsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            2 -> {
                // Tab Planes: Gestión de planes/actividades
                val plansViewModel: PlansViewModel = viewModel()
                PlansScreen(
                    viewModel = plansViewModel,
                    onNavigateBack = { /* No action needed in tab */ },
                    onNavigateToAddPlan = {
                        // TODO: Navegar a agregar plan
                    }
                )
            }
            3 -> {
                // Tab Reportes: Análisis y reportes financieros
                ReportsScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun ModernBottomBar(
    items: List<String>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedItem == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (isSelected)
                                    WayraOrange.copy(alpha = 0.15f)
                                else
                                    androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            val iconSize by animateDpAsState(
                                targetValue = if (isSelected) 26.dp else 22.dp,
                                animationSpec = tween(durationMillis = 300),
                                label = "iconSize"
                            )

                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.home
                                        1 -> R.drawable.group_running
                                        2 -> R.drawable.plan
                                        else -> R.drawable.payment
                                    }
                                ),
                                contentDescription = item,
                                tint = if (isSelected) WayraOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(iconSize)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item,
                                fontSize = if (isSelected) 11.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) WayraOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}