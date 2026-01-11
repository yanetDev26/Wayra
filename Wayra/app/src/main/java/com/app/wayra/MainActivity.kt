package com.app.wayra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
                    studentsViewModel = studentsViewModel
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
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) WayraOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}