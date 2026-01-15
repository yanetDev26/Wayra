package com.app.wayra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.wayra.ui.home.HomeScreen
import com.app.wayra.ui.home.HomeViewModel
import com.app.wayra.ui.payments.PaymentDetailScreen
import com.app.wayra.ui.payments.PaymentViewModel
import com.app.wayra.ui.payments.RegisterPaymentScreen

sealed class HomeScreens(val route: String) {
    object Home : HomeScreens("home")
    object RegisterPayment : HomeScreens("register_payment")
    object PaymentDetail : HomeScreens("payment_detail/{paymentId}")
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreens.Home.route,
        modifier = modifier
    ) {
        // Pantalla principal: Home Dashboard
        composable(HomeScreens.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRegisterPayment = {
                    navController.navigate(HomeScreens.RegisterPayment.route)
                }
            )
        }

        // Pantalla: Registrar pago
        composable(HomeScreens.RegisterPayment.route) {
            val paymentViewModel: PaymentViewModel = viewModel()
            RegisterPaymentScreen(
                viewModel = paymentViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentRegistered = {
                    homeViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Detalle de pago
        composable(
            route = HomeScreens.PaymentDetail.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            val paymentViewModel: PaymentViewModel = viewModel()
            PaymentDetailScreen(
                paymentId = paymentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = paymentViewModel
            )
        }
    }
}
