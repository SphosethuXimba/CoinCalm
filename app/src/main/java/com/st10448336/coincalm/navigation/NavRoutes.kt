package com.st10448336.coincalm.navigation

// Attribution: Jetpack Compose Navigation Routes
// Link: https://developer.android.com/jetpack/compose/navigation
// Author: Android Developers
sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Dashboard : NavRoutes("dashboard")
    object AddCategory : NavRoutes("add_category")
    object AddExpense : NavRoutes("add_expense")
    object BudgetGoals : NavRoutes("budget_goals")
    object Reports : NavRoutes("reports")
    object Camera : NavRoutes("camera")
    object Settings : NavRoutes("settings")
    object Badges : NavRoutes("badges")
    object EditProfile : NavRoutes("edit_profile")
    object ForgotPassword  : NavRoutes("forgot_password")
}