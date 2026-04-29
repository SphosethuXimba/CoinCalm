package com.st10448336.coincalm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.data.entites.User
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    val isDark  = LocalDarkMode.current

    var currentUser    by remember { mutableStateOf<User?>(null) }
    var totalSpent     by remember { mutableStateOf(0f) }
    var totalIncome    by remember { mutableStateOf(0f) }
    var maxGoalAmount  by remember { mutableStateOf<Float?>(null) }
    var recentExpenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var categoryNames  by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(navController.currentBackStackEntry) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Dashboard.route) { inclusive = true } }
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val user = db.userDao().getUserById(uid)

            val cal       = Calendar.getInstance()
            val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            val monthStart = "$yearMonth-01"
            val monthEnd   = "$yearMonth-31"

            val spent  = db.expenseDao().getTotalSpentThisMonth(uid, yearMonth)
            val recent = db.expenseDao().getExpensesForPeriod(uid, monthStart, monthEnd).take(10)
            val cats   = db.categoryDao().getCategoriesForUser(uid).first()
            val catMap = cats.associate { it.categoryId to it.categoryName }
            val currentGoal = db.goalDao().getGoalForMonth(uid, yearMonth)

            withContext(Dispatchers.Main) {
                currentUser    = user
                totalSpent     = spent
                totalIncome    = user?.monthlyIncome ?: 0f
                maxGoalAmount  = currentGoal?.maxGoalAmount
                recentExpenses = recent
                categoryNames  = catMap
            }
        }
    }

    val currency = currentUser?.currencyPreference ?: "R"
    val balance  = totalIncome - totalSpent
    val isOverBudget = maxGoalAmount != null && totalSpent > maxGoalAmount!!

    Column(modifier = Modifier.fillMaxSize().background(screenBackground()).verticalScroll(rememberScrollState())) {

        // Greeting
        Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("GREETINGS,", color = contentSecondary(), fontSize = 13.sp)
                Text(
                    text = currentUser?.username?.ifBlank { currentUser!!.email.substringBefore("@") } ?: "Loading…",
                    color = contentPrimary(), fontSize = 22.sp, fontWeight = FontWeight.Bold
                )
            }
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(LimeGreen), contentAlignment = Alignment.Center) {
                Text((currentUser?.username ?: "?").take(2).uppercase(), color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // Balance Card
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total Balance:", color = contentSecondary(), fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("$currency ${"%.2f".format(balance)}", color = contentPrimary(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyLight else Color(0xFFF5F8FF))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("↑ Income", color = contentSecondary(), fontSize = 11.sp)
                            Text("$currency ${"%.2f".format(totalIncome)}", color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isOverBudget) ErrorRed.copy(alpha = 0.1f) else if (isDark) NavyLight else Color(0xFFF5F8FF))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("↓ Expenses", color = if (isOverBudget) ErrorRed else contentSecondary(), fontSize = 11.sp)
                            Text("$currency ${"%.2f".format(totalSpent)}", color = if (isOverBudget) ErrorRed else Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (isOverBudget) {
                                Text("⚠️ Over Budget!", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                            } else if (maxGoalAmount != null) {
                                Text("Limit: $currency ${"%.2f".format(maxGoalAmount)}", color = contentSecondary(), fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick Actions
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionButton("Expenses", Color(0xFF1B3A6B), "💸", { navController.navigate(NavRoutes.AddExpense.route) }, Modifier.weight(1f), isDark)
                QuickActionButton("Report", Color(0xFF2E4A2E), "📊", { navController.navigate(NavRoutes.Reports.route) }, Modifier.weight(1f), isDark)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionButton("Goals", Color(0xFF1B4A3A), "🎯", { navController.navigate(NavRoutes.BudgetGoals.route) }, Modifier.weight(1f), isDark)
                QuickActionButton("Badges", Color(0xFF4A3A1B), "🏆", { navController.navigate(NavRoutes.Badges.route) }, Modifier.weight(1f), isDark)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Transactions Header
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("RECENT TRANSACTIONS", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { navController.navigate(NavRoutes.Reports.route) }) { Text("See All", color = LimeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Card(modifier = Modifier.clickable { navController.navigate(NavRoutes.AddCategory.route) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = LimeGreen)) {
                    Text("+ Category", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (recentExpenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No transactions yet.\nTap + to add your first expense.", color = contentSecondary(), fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        } else {
            recentExpenses.forEach { expense ->
                TransactionRow(expense, categoryNames[expense.categoryId] ?: "Uncategorised", currency)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QuickActionButton(label: String, bgColor: Color, textIcon: String, onClick: () -> Unit, modifier: Modifier = Modifier, isDark: Boolean) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(bgColor), contentAlignment = Alignment.Center) {
                Text(textIcon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(label, color = contentPrimary(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TransactionRow(expense: Expense, categoryName: String, currency: String) {
    val displayDate = remember(expense.date, expense.startTime) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expDate = sdf.parse(expense.date)
            val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
            if (expDate != null && expDate >= todayStart) "Today, ${expense.startTime}" else SimpleDateFormat("MMM dd", Locale.getDefault()).format(expDate ?: Date())
        } catch (_: Exception) { expense.date }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(categoryColor(categoryName)), contentAlignment = Alignment.Center) {
                Text(categoryName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(categoryName, color = contentPrimary(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(displayDate, color = contentSecondary(), fontSize = 12.sp)
            }
        }
        Text("-$currency ${"%.2f".format(expense.amount)}", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

private fun categoryColor(name: String): Color = when {
    name.contains("grocer", ignoreCase = true) -> Color(0xFF2E7D32)
    name.contains("food", ignoreCase = true) -> Color(0xFFE65100)
    name.contains("fuel", ignoreCase = true) -> Color(0xFF1565C0)
    name.contains("transport", ignoreCase = true) -> Color(0xFF6A1B9A)
    name.contains("salary", ignoreCase = true) -> Color(0xFF00695C)
    name.contains("rent", ignoreCase = true) -> Color(0xFF4E342E)
    name.contains("util", ignoreCase = true) -> Color(0xFFF9A825)
    name.contains("health", ignoreCase = true) -> Color(0xFFC62828)
    name.contains("entertain", ignoreCase = true) -> Color(0xFF283593)
    else -> Color(0xFF37474F)
}