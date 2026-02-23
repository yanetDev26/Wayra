package com.app.wayra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.wayra.ui.home.ActiveStudentsScreen
import com.app.wayra.ui.home.HomeScreen
import com.app.wayra.ui.home.HomeViewModel
import com.app.wayra.ui.home.NewStudentsScreen
import com.app.wayra.ui.home.PendingPaymentsScreen
import com.app.wayra.ui.payments.PaymentDetailScreen
import com.app.wayra.ui.payments.PaymentViewModel
import com.app.wayra.ui.payments.RegisterPaymentScreen

sealed class HomeScreens(val route: String) {
    object Home : HomeScreens("home")
    object RegisterPayment : HomeScreens("register_payment")
    object PaymentDetail : HomeScreens("payment_detail/{paymentId}")
    object PendingPayments : HomeScreens("pending_payments/{periodType}")
    object ActiveStudents : HomeScreens("active_students")
    object NewStudents : HomeScreens("new_students")
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
                },
                onNavigateToActiveStudents = {
                    navController.navigate(HomeScreens.ActiveStudents.route)
                },
                onNavigateToNewStudents = {
                    navController.navigate(HomeScreens.NewStudents.route)
                },
                onNavigateToPendingThisMonth = {
                    navController.navigate("pending_payments/this_month")
                },
                onNavigateToPendingLastMonth = {
                    navController.navigate("pending_payments/last_month")
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

        // Pantalla: Pagos pendientes
        composable(
            route = HomeScreens.PendingPayments.route,
            arguments = listOf(navArgument("periodType") { type = NavType.StringType })
        ) { backStackEntry ->
            val periodType = backStackEntry.arguments?.getString("periodType") ?: "this_month"
            PendingPaymentsScreen(
                periodType = periodType,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Estudiantes activos
        composable(HomeScreens.ActiveStudents.route) {
            ActiveStudentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Estudiantes nuevos
        composable(HomeScreens.NewStudents.route) {
            NewStudentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
