package com.st10448336.coincalm.navigation

/**
 * Sealed class of all navigation route strings used by the NavHost.
 * Using a sealed class prevents typos — all routes are defined exactly once.
 */
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
}