package com.app.wayra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.wayra.ui.students.AddStudentScreen
import com.app.wayra.ui.students.StudentDetailScreen
import com.app.wayra.ui.students.StudentsScreen
import com.app.wayra.ui.students.StudentsViewModel

sealed class StudentsScreens(val route: String) {
    object StudentsList : StudentsScreens("students_list")
    object AddStudent : StudentsScreens("add_student")
    object StudentDetail : StudentsScreens("student_detail/{studentId}") {
        fun createRoute(studentId: String) = "student_detail/$studentId"
    }
}

@Composable
fun StudentsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    studentsViewModel: StudentsViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = StudentsScreens.StudentsList.route,
        modifier = modifier
    ) {
        // Pantalla principal: Lista de estudiantes
        composable(StudentsScreens.StudentsList.route) {
            StudentsScreen(
                viewModel = studentsViewModel,
                onNavigateBack = { /* No hacer nada, estamos en una tab */ },
                onNavigateToAddStudent = {
                    navController.navigate(StudentsScreens.AddStudent.route)
                },
                onNavigateToStudentDetail = { studentId ->
                    navController.navigate(StudentsScreens.StudentDetail.createRoute(studentId))
                },
                showTopBar = false, // No mostrar topBar en la tab
                modifier = modifier
            )
        }

        // Pantalla: Agregar estudiante
        composable(StudentsScreens.AddStudent.route) {
            AddStudentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStudentAdded = {
                    studentsViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Detalle/Editar/Eliminar estudiante
        composable(
            route = StudentsScreens.StudentDetail.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentDetailScreen(
                studentId = studentId,
                onNavigateBack = {
                    studentsViewModel.refreshData()
                    navController.popBackStack()
                },
                viewModel = studentsViewModel
            )
        }
    }
}
