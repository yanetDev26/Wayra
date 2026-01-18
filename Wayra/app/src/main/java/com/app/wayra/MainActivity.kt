package com.app.wayra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.app.wayra.navigation.HomeNavHost
import com.app.wayra.navigation.PlansNavHost
import com.app.wayra.navigation.ReportsNavHost
import com.app.wayra.navigation.StudentsNavHost
import com.app.wayra.ui.home.HomeViewModel
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.splash.SplashScreen
import com.app.wayra.ui.students.StudentsViewModel
import com.app.wayra.ui.theme.Cream
import com.app.wayra.ui.theme.Gray
import com.app.wayra.ui.theme.Orange
import com.app.wayra.ui.theme.WayraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WayraTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Inicio", "Alumnos", "Planes", "Reportes")

    // ViewModels compartidos para mantener el estado
    val homeViewModel: HomeViewModel = viewModel()
    val studentsViewModel: StudentsViewModel = viewModel()
    val plansViewModel: PlansViewModel = viewModel()

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
                val homeNavController = rememberNavController()
                HomeNavHost(
                    navController = homeNavController,
                    homeViewModel = homeViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            1 -> {
                val studentsNavController = rememberNavController()
                StudentsNavHost(
                    navController = studentsNavController,
                    studentsViewModel = studentsViewModel,
                    homeViewModel = homeViewModel
                )
            }
            2 -> {
                val plansNavController = rememberNavController()
                PlansNavHost(
                    navController = plansNavController,
                    plansViewModel = plansViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            3 -> {
                val reportsNavController = rememberNavController()
                ReportsNavHost(
                    navController = reportsNavController
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
        color = Gray,
        shadowElevation = 4.dp
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
                        .clickable(
                            onClick = { onItemSelected(index) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = when (index) {
                                0 -> R.drawable.home
                                1 -> R.drawable.runner
                                2 -> R.drawable.plan
                                else -> R.drawable.report
                            }
                        ),
                        contentDescription = item,
                        tint = if (isSelected) Orange else Cream,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Orange else Cream,
                        maxLines = 1
                    )
                }
            }
        }
    }
}