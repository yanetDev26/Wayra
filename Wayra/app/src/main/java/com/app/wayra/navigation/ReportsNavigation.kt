package com.app.wayra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.wayra.ui.reports.IncomeProjectionReportScreen
import com.app.wayra.ui.reports.MonthlyIncomeReportScreen
import com.app.wayra.ui.reports.PaymentHistoryReportScreen
import com.app.wayra.ui.reports.PendingPaymentsReportScreen
import com.app.wayra.ui.reports.PlanStatisticsReportScreen
import com.app.wayra.ui.reports.ReportsScreen
import com.app.wayra.ui.reports.ReportsViewModel

sealed class ReportsScreens(val route: String) {
    object Reports : ReportsScreens("reports")
    object MonthlyIncomeReport : ReportsScreens("monthly_income_report")
    object PendingPaymentsReport : ReportsScreens("pending_payments_report")
    object PlanStatisticsReport : ReportsScreens("plan_statistics_report")
    object PaymentHistoryReport : ReportsScreens("payment_history_report")
    object IncomeProjectionReport : ReportsScreens("income_projection_report")
}

@Composable
fun ReportsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ReportsScreens.Reports.route,
        modifier = modifier
    ) {
        // Pantalla principal: Lista de reportes
        composable(ReportsScreens.Reports.route) {
            ReportsScreen(
                onNavigateToMonthlyIncome = {
                    navController.navigate(ReportsScreens.MonthlyIncomeReport.route)
                },
                onNavigateToPendingPayments = {
                    navController.navigate(ReportsScreens.PendingPaymentsReport.route)
                },
                onNavigateToPlanStatistics = {
                    navController.navigate(ReportsScreens.PlanStatisticsReport.route)
                },
                onNavigateToPaymentHistory = {
                    navController.navigate(ReportsScreens.PaymentHistoryReport.route)
                },
                onNavigateToIncomeProjection = {
                    navController.navigate(ReportsScreens.IncomeProjectionReport.route)
                }
            )
        }

        // Pantalla: Reporte de Ingresos Mensuales
        composable(ReportsScreens.MonthlyIncomeReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            MonthlyIncomeReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Reporte de Pagos Pendientes
        composable(ReportsScreens.PendingPaymentsReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            PendingPaymentsReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Estadísticas por Plan
        composable(ReportsScreens.PlanStatisticsReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            PlanStatisticsReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Historial de Pagos
        composable(ReportsScreens.PaymentHistoryReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            PaymentHistoryReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Proyección de Ingresos
        composable(ReportsScreens.IncomeProjectionReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            IncomeProjectionReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
