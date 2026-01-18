package com.app.wayra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.wayra.ui.plans.AddPlanScreen
import com.app.wayra.ui.plans.PlansScreen
import com.app.wayra.ui.plans.PlansViewModel

sealed class PlansScreens(val route: String) {
    object Plans : PlansScreens("plans")
    object AddPlan : PlansScreens("add_plan")
}

@Composable
fun PlansNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    plansViewModel: PlansViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = PlansScreens.Plans.route
    ) {
        // Pantalla principal: Lista de planes
        composable(PlansScreens.Plans.route) {
            PlansScreen(
                viewModel = plansViewModel,
                onNavigateToAddPlan = {
                    navController.navigate(PlansScreens.AddPlan.route)
                },
                modifier = modifier
            )
        }

        // Pantalla: Agregar plan
        composable(PlansScreens.AddPlan.route) {
            AddPlanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlanAdded = {
                    plansViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }
    }
}
