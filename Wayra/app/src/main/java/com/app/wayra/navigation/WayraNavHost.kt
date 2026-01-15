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
import com.app.wayra.ui.students.StudentsScreen
import com.app.wayra.ui.students.StudentsViewModel
import com.app.wayra.ui.students.AddStudentScreen
import com.app.wayra.ui.students.StudentDetailScreen
import com.app.wayra.ui.plans.PlansScreen
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.plans.AddPlanScreen
import com.app.wayra.ui.payments.RegisterPaymentScreen
import com.app.wayra.ui.payments.PaymentDetailScreen
import com.app.wayra.ui.payments.PaymentViewModel
import com.app.wayra.ui.subscriptions.AssignPlanScreen
import com.app.wayra.ui.reports.ReportsScreen
import com.app.wayra.ui.reports.ReportsViewModel
import com.app.wayra.ui.reports.MonthlyIncomeReportScreen

@Composable
fun WayraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRegisterPayment = { navController.navigate(Screen.RegisterPayment.route) }
            )
        }

        composable(Screen.Students.route) {
            val viewModel: StudentsViewModel = viewModel()
            StudentsScreen(
                viewModel = viewModel,
                onNavigateToAddStudent = { navController.navigate(Screen.AddStudent.route) },
                onNavigateToStudentDetail = { studentId ->
                    navController.navigate(Screen.StudentDetail.createRoute(studentId))
                }
            )
        }

        composable(Screen.AddStudent.route) {
            AddStudentScreen(
                onNavigateBack = { navController.popBackStack() },
                onStudentAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StudentDetail.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentDetailScreen(
                studentId = studentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Plans.route) {
            val viewModel: PlansViewModel = viewModel()
            PlansScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPlan = { navController.navigate(Screen.AddPlan.route) }
            )
        }

        composable(Screen.AddPlan.route) {
            AddPlanScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlanAdded = { navController.popBackStack() }
            )
        }

        composable(Screen.AssignPlan.route) {
            AssignPlanScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlanAssigned = { navController.popBackStack() }
            )
        }

        composable(Screen.RegisterPayment.route) {
            val viewModel: PaymentViewModel = viewModel()
            RegisterPaymentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPaymentRegistered = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PaymentDetail.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            val viewModel: PaymentViewModel = viewModel()
            PaymentDetailScreen(
                paymentId = paymentId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateToMonthlyIncome = { navController.navigate(Screen.MonthlyIncomeReport.route) }
            )
        }

        composable(Screen.MonthlyIncomeReport.route) {
            val viewModel: ReportsViewModel = viewModel()
            MonthlyIncomeReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
