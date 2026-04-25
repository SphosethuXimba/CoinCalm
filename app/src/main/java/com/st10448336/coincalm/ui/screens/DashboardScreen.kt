package com.st10448336.coincalm.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.User
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDark
import com.st10448336.coincalm.ui.theme.NavyLight
import com.st10448336.coincalm.ui.theme.NavyMedium
import com.st10448336.coincalm.ui.theme.TextPrimary
import com.st10448336.coincalm.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DashboardScreen — Navigation hub and spending overview.
 *
 * SKELETON: Gamification badge and monthly spending total are placeholders.
 *
 * TODO (Team): Replace placeholder XP / level with real gamification DB query.
 * TODO (Team): Replace R0.00 placeholder with ExpenseDao.getTotalSpentThisMonth().
 * TODO (Team): Replace progress bar value with (totalSpent / maxGoalAmount) * 100.
 *
 * @author Sphosethu Ximba [ST10448336] — PROG7313 POE Part 2
 */
@Composable
fun DashboardScreen(navController: NavController) {

    val TAG     = "DashboardScreen"
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val auth    = FirebaseAuth.getInstance()

    // ── Compose State ──────────────────────────────────────────────────────
    var currentUser by remember { mutableStateOf<User?>(null) }
    var xpProgress  by remember { mutableStateOf(0.35f) } // Placeholder 35%

    // ── Load user profile from RoomDB on first composition ─────────────────
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No Firebase session on Dashboard — redirecting to Login")
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val user = db.userDao().getUserById(uid)
            Log.d(TAG, "User profile loaded: ${user?.username}")

            // TODO (Team): Load monthly total with db.expenseDao().getTotalSpentThisMonth(uid, currentMonth)
            Log.d(TAG, "TODO (Team): Load monthly spending total from ExpenseDao for UID: $uid")

            withContext(Dispatchers.Main) { currentUser = user }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text  = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text  = currentUser?.username ?: "...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text  = currentUser?.currencyPreference ?: "ZAR",
                    color = LimeGreen,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = {
                auth.signOut()
                Log.d(TAG, "User signed out")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                }
            }) {
                Text("Logout", color = LimeGreen)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Gamification Card ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = NavyMedium)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text      = "🏅  Your Progress",
                    color     = LimeGreen,
                    fontSize  = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Level 1 — Budget Beginner",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(10.dp))
                // XP progress bar
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color            = LimeGreen,
                    trackColor       = NavyLight,
                )
                Text(
                    text  = "35 / 100 XP to Level 2",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Monthly Budget Card ────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = NavyMedium)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text  = "THIS MONTH",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                // TODO (Team): Replace "R 0.00" with real total from ExpenseDao
                Text(
                    text  = "R 0.00 spent",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color      = LimeGreen,
                    trackColor = NavyLight,
                )
                // TODO (Team): Replace with real goal from GoalDao
                Text(
                    text  = "Set a budget goal to track progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "QUICK ACTIONS",
            color = TextSecondary,
            fontSize = 11.sp,
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ── Navigation Cards ───────────────────────────────────────────────
        NavCard(
            emoji       = "➕",
            title       = "Add Expense",
            subtitle    = "Log a purchase with amount, category and receipt",
            onClick     = { navController.navigate(NavRoutes.AddExpense.route) }
        )
        NavCard(
            emoji       = "🗂️",
            title       = "Categories",
            subtitle    = "Create and manage spending categories",
            onClick     = { navController.navigate(NavRoutes.AddCategory.route) }
        )
        NavCard(
            emoji       = "🎯",
            title       = "Budget Goals",
            subtitle    = "Set your minimum and maximum monthly limits",
            onClick     = { navController.navigate(NavRoutes.BudgetGoals.route) }
        )
        NavCard(
            emoji       = "📊",
            title       = "Reports & History",
            subtitle    = "View spending by period and category",
            onClick     = { navController.navigate(NavRoutes.Reports.route) }
        )

        Spacer(Modifier.height(16.dp))
    }
}

/** Reusable clickable card for dashboard quick-action navigation. */
@Composable
private fun NavCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "$emoji  $title",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text     = subtitle,
                style    = MaterialTheme.typography.bodyMedium,
                color    = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}