package com.app.wayra.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Students : Screen("students")
    object StudentDetail : Screen("student_detail/{studentId}") {
        fun createRoute(studentId: String) = "student_detail/$studentId"
    }
    object AddStudent : Screen("add_student")
    object Plans : Screen("plans")
    object PlanDetail : Screen("plan_detail/{planId}") {
        fun createRoute(planId: String) = "plan_detail/$planId"
    }
    object AddPlan : Screen("add_plan")
    object AssignPlan : Screen("assign_plan")
    object RegisterPayment : Screen("register_payment")
    object PaymentDetail : Screen("payment_detail/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_detail/$paymentId"
    }
    object Reports : Screen("reports")
}
