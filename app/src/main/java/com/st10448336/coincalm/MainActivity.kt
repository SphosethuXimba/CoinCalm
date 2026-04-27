package com.st10448336.coincalm

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.components.BottomNavBar
import com.st10448336.coincalm.ui.screens.*
import com.st10448336.coincalm.ui.theme.CoinCalmTheme
import com.st10448336.coincalm.ui.theme.NavyDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 REQUEST NOTIFICATION PERMISSION (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }


        setContent {
            CoinCalmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyDark
                ) {
                    CoinCalmApp()
                }
            }
        }
    }
}

@Composable
fun CoinCalmApp() {

    val navController = rememberNavController()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != NavRoutes.Login.route &&
                currentRoute != NavRoutes.Register.route &&
                currentRoute != NavRoutes.Splash.route
            ) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(NavRoutes.Splash.route) { SplashScreen(navController) }
            composable(NavRoutes.Login.route) { LoginScreen(navController) }
            composable(NavRoutes.Register.route) { RegisterScreen(navController) }


            composable(NavRoutes.Login.route) {
                LoginScreen(navController)
            }

            composable(NavRoutes.Register.route) {
                RegisterScreen(navController)
            }

            // ── MAIN APP ─────────────────────────
            composable(NavRoutes.Dashboard.route) {
                DashboardScreen(navController)
            }

            composable(NavRoutes.AddCategory.route) {
                AddCategoryScreen(navController)
            }

            composable(NavRoutes.AddExpense.route) {
                AddExpenseScreen(navController)
            }

            composable(NavRoutes.BudgetGoals.route) {
                BudgetGoalsScreen(navController)
            }

            composable(NavRoutes.Reports.route) {
                ReportsHistoryScreen(navController)
            }

            composable(NavRoutes.Camera.route) {
                CameraScreen(navController)
            }
            composable(NavRoutes.Settings.route) {
                SettingsScreen(navController = navController)
            }

            composable(NavRoutes.Badges.route) { BadgesScreen(navController) }

            composable(NavRoutes.Dashboard.route) { DashboardScreen(navController) }
            composable(NavRoutes.AddCategory.route) { AddCategoryScreen(navController) }
            composable(NavRoutes.AddExpense.route) { AddExpenseScreen(navController) }
            composable(NavRoutes.BudgetGoals.route) { BudgetGoalsScreen(navController) }
            composable(NavRoutes.Reports.route) { ReportsHistoryScreen(navController) }
            composable(NavRoutes.Camera.route) { CameraScreen(navController) }
            composable(NavRoutes.Settings.route) { SettingsScreen(navController = navController) }

        }
    }
}