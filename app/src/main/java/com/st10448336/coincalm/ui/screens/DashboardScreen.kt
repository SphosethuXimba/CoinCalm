package com.st10448336.coincalm.ui.screens

import android.util.Log
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
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDark
import com.st10448336.coincalm.ui.theme.NavyLight
import com.st10448336.coincalm.ui.theme.NavyMedium
import com.st10448336.coincalm.ui.theme.TextPrimary
import com.st10448336.coincalm.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavController) {

    val tag     = "DashboardScreen"
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()

    var currentUser    by remember { mutableStateOf<User?>(null) }
    var totalSpent     by remember { mutableStateOf(0f) }
    var totalIncome    by remember { mutableStateOf(0f) }
    var recentExpenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var categoryNames  by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(navController.currentBackStackEntry) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(tag, "No Firebase session — redirecting to Login")
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val user = db.userDao().getUserById(uid)

            val cal       = Calendar.getInstance()
            val yearMonth = "%04d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1
            )
            val monthStart = "$yearMonth-01"
            val monthEnd   = "$yearMonth-31"

            val spent  = db.expenseDao().getTotalSpentThisMonth(uid, yearMonth)
            val recent = db.expenseDao()
                .getExpensesForPeriod(uid, monthStart, monthEnd)
                .take(10)

            val cats   = db.categoryDao().getCategoriesForUser(uid).first()
            val catMap = cats.associate { it.categoryId to it.categoryName }

            Log.d(tag, "Loaded ${recent.size} expenses | spent: $spent")

            withContext(Dispatchers.Main) {
                currentUser    = user
                totalSpent     = spent
                totalIncome    = 18000f
                recentExpenses = recent
                categoryNames  = catMap
            }
        }
    }

    val currency = currentUser?.currencyPreference ?: "R"
    val balance  = totalIncome - totalSpent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Greeting row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text     = "Good Morning,",
                    color    = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text       = currentUser?.username ?: "...",
                    color      = TextPrimary,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LimeGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = (currentUser?.username ?: "?").take(2).uppercase(),
                    color      = NavyDark,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
            }
        }

        // ── Balance card ───────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyMedium)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Total Balance:", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "$currency ${"%.2f".format(balance)}",
                    color      = TextPrimary,
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "↑ Income", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text       = "$currency ${"%.2f".format(totalIncome)}",
                                color      = LimeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "↓ Expenses", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text       = "$currency ${"%.2f".format(totalSpent)}",
                                color      = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── 4 Quick action buttons (2x2 grid) ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label    = "Expenses",
                    bgColor  = Color(0xFF1B3A6B),
                    textIcon = "EXP",
                    onClick  = { navController.navigate(NavRoutes.AddExpense.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label    = "Report",
                    bgColor  = Color(0xFF2E4A2E),
                    textIcon = "RPT",
                    onClick  = { navController.navigate(NavRoutes.Reports.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label    = "Goals",
                    bgColor  = Color(0xFF1B4A3A),
                    textIcon = "AIM",
                    onClick  = { navController.navigate(NavRoutes.BudgetGoals.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label    = "Badges",
                    bgColor  = Color(0xFF4A3A1B),
                    textIcon = "XP",
                    onClick  = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Recent transactions header + Category button ───────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = "RECENT TRANSACTIONS",
                color         = TextSecondary,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
            Card(
                modifier = Modifier.clickable {
                    navController.navigate(NavRoutes.AddCategory.route)
                },
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LimeGreen)
            ) {
                Text(
                    text       = "+ Category",
                    color      = NavyDark,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Transaction list ───────────────────────────────────────────────
        if (recentExpenses.isEmpty()) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "No transactions yet.\nTap + to add your first expense.",
                    color     = TextSecondary,
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            recentExpenses.forEach { expense ->
                TransactionRow(
                    expense      = expense,
                    categoryName = categoryNames[expense.categoryId] ?: "Uncategorised",
                    currency     = currency
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ── Quick action button ────────────────────────────────────────────────────────
@Composable
private fun QuickActionButton(
    label: String,
    bgColor: Color,
    textIcon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = textIcon,
                    color      = LimeGreen,
                    fontSize   = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text       = label,
                color      = TextPrimary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Single transaction row ─────────────────────────────────────────────────────
@Composable
private fun TransactionRow(
    expense: Expense,
    categoryName: String,
    currency: String
) {
    val displayDate = remember(expense.date, expense.startTime) {
        try {
            val sdf        = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expDate    = sdf.parse(expense.date)
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            if (expDate != null && expDate >= todayStart) {
                "Today, ${expense.startTime}"
            } else {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(expDate ?: Date())
            }
        } catch (_: Exception) {
            expense.date
        }
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(categoryColor(categoryName)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = categoryName.take(1).uppercase(),
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text       = categoryName,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Text(
                    text     = displayDate,
                    color    = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text       = "-$currency ${"%.2f".format(expense.amount)}",
            color      = Color(0xFFFF6B6B),
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
    }
}

// ── Category → colour mapping ──────────────────────────────────────────────────
private fun categoryColor(name: String): Color = when {
    name.contains("grocer",    ignoreCase = true) -> Color(0xFF2E7D32)
    name.contains("food",      ignoreCase = true) -> Color(0xFFE65100)
    name.contains("fuel",      ignoreCase = true) -> Color(0xFF1565C0)
    name.contains("transport", ignoreCase = true) -> Color(0xFF6A1B9A)
    name.contains("salary",    ignoreCase = true) -> Color(0xFF00695C)
    name.contains("rent",      ignoreCase = true) -> Color(0xFF4E342E)
    name.contains("util",      ignoreCase = true) -> Color(0xFFF9A825)
    name.contains("health",    ignoreCase = true) -> Color(0xFFC62828)
    name.contains("entertain", ignoreCase = true) -> Color(0xFF283593)
    else                                           -> Color(0xFF37474F)
}