package com.st10448336.coincalm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.screens.AddCategoryScreen
import com.st10448336.coincalm.ui.screens.AddExpenseScreen
import com.st10448336.coincalm.ui.screens.BudgetGoalsScreen
import com.st10448336.coincalm.ui.screens.DashboardScreen
import com.st10448336.coincalm.ui.screens.LoginScreen
import com.st10448336.coincalm.ui.screens.RegisterScreen
import com.st10448336.coincalm.ui.screens.ReportsHistoryScreen
import com.st10448336.coincalm.ui.screens.SplashScreen
import com.st10448336.coincalm.ui.theme.CoinCalmTheme
import com.st10448336.coincalm.ui.theme.NavyDark

/**
 * MainActivity — single-activity host for the entire Compose UI.
 *
 * The [NavHost] controls all screen routing. Firebase Auth's current session
 * determines the start destination: already logged in → Dashboard; else → Login.
 *
 * @author CoinCalm Team — PROG7313 POE Part 2
 */
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate — checking Firebase session")

        setContent {
            CoinCalmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = NavyDark
                ) {
                    CoinCalmApp()
                }
            }
        }
    }
}

@Composable
fun CoinCalmApp() {
    val navController: NavHostController = rememberNavController()

    // If Firebase already has an authenticated user, skip login entirely
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Log.d("CoinCalmApp", "Active Firebase session → starting at Dashboard")
        NavRoutes.Dashboard.route
    } else {
        Log.d("CoinCalmApp", "No session → starting at Login")
        NavRoutes.Login.route
    }

    NavHost(
        navController    = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(NavRoutes.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(NavRoutes.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(NavRoutes.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(NavRoutes.AddCategory.route) {
            AddCategoryScreen(navController = navController)
        }
        composable(NavRoutes.AddExpense.route) {
            AddExpenseScreen(navController = navController)
        }
        composable(NavRoutes.BudgetGoals.route) {
            BudgetGoalsScreen(navController = navController)
        }
        composable(NavRoutes.Reports.route) {
            ReportsHistoryScreen(navController = navController)
        }
    }
}