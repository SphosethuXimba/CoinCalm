package com.st10448336.coincalm.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.st10448336.coincalm.data.dao.CategoryTotal
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.ui.theme.ErrorRed
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDark
import com.st10448336.coincalm.ui.theme.NavyDarkest
import com.st10448336.coincalm.ui.theme.NavyMedium
import com.st10448336.coincalm.ui.theme.TextPrimary
import com.st10448336.coincalm.ui.theme.TextSecondary
import com.st10448336.coincalm.ui.theme.coinCalmTextFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * ReportsHistoryScreen — REQUIREMENT-05: Period-based filtering and spending history.
 *
 * SKELETON: Date pickers are functional. RecyclerView (LazyColumn) placeholders
 * show exactly where to wire in DB queries and Coil image loading.
 *
 * TODO (Team): Wire up LazyColumn for category totals with ExpenseDao.getCategoryTotalsForPeriod
 * TODO (Team): Wire up LazyColumn for expense list with ExpenseDao.getExpensesForPeriod
 * TODO (Team): In expense list items, load receipt images with Coil:
 *   AsyncImage(
 *       model             = expense.supabaseImageUrl,
 *       contentDescription = "Receipt",
 *       placeholder       = painterResource(R.drawable.ic_receipt_placeholder),
 *       error             = painterResource(R.drawable.ic_receipt_error),
 *       modifier          = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
 *   )
 *
 * @author Sphosethu Ximba [ST10448336] — PROG7313 POE Part 2
 */
@Composable
fun ReportsHistoryScreen(navController: NavController) {

    val TAG     = "ReportsScreen"
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Compose State ──────────────────────────────────────────────────────
    var startDate     by remember { mutableStateOf("") }
    var endDate       by remember { mutableStateOf("") }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }
    var expenses      by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var categoryTotals by remember { mutableStateOf<List<CategoryTotal>>(emptyList()) }
    var grandTotal    by remember { mutableStateOf(0f) }
    var periodSummary by remember { mutableStateOf("Select a date range and tap Apply") }

    // Pre-fill with first day of current month → today
    LaunchedEffect(Unit) {
        val cal    = Calendar.getInstance()
        val today  = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        startDate = firstDay
        endDate   = today
    }

    val isApplyEnabled = startDate.isNotBlank() && endDate.isNotBlank() && !isLoading

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDark,
        topBar = {
            CoinCalmTopBar(title = "Reports & History", onBack = { navController.popBackStack() })
        }
    ) { padding ->

        // Use a Column wrapping a LazyColumn so the date pickers scroll with content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Date range pickers ─────────────────────────────────────────
            Text(
                text  = "SELECT DATE RANGE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start date
                OutlinedTextField(
                    value         = startDate,
                    onValueChange = {},
                    label         = { Text("Start Date") },
                    isError       = startDateError != null,
                    readOnly      = true,
                    colors        = coinCalmTextFieldColors(),
                    modifier      = Modifier.weight(1f),
                    trailingIcon  = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                    startDateError = null
                                    Log.d(TAG, "Start date: $startDate")
                                },
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { Text("Pick", color = LimeGreen) }
                    }
                )
                // End date
                OutlinedTextField(
                    value         = endDate,
                    onValueChange = {},
                    label         = { Text("End Date") },
                    readOnly      = true,
                    colors        = coinCalmTextFieldColors(),
                    modifier      = Modifier.weight(1f),
                    trailingIcon  = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                    Log.d(TAG, "End date: $endDate")
                                },
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { Text("Pick", color = LimeGreen) }
                    }
                )
            }

            startDateError?.let {
                Text(it, color = ErrorRed, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(12.dp))

            // ── Apply Filter Button ────────────────────────────────────────
            Button(
                onClick = {
                    // Validate date range
                    if (startDate > endDate) {
                        startDateError = "Start date cannot be after end date"
                        return@Button
                    }
                    startDateError = null

                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        scope.launch { snackbarHostState.showSnackbar("Session error — please log in again") }
                        return@Button
                    }

                    isLoading = true
                    Log.d(TAG, "Loading reports for UID: $uid | $startDate → $endDate")

                    scope.launch(Dispatchers.IO) {
                        // ── TODO (Team): Load expense list ────────────────────────────
                        // Uncomment when Expense records are being inserted:
                        Log.d(TAG, "TODO (Team): Query ExpenseDao.getExpensesForPeriod($uid, $startDate, $endDate)")
                        val expenseList = AppDatabase.getInstance(context)
                            .expenseDao()
                            .getExpensesForPeriod(uid, startDate, endDate)

                        // ── TODO (Team): Load category totals ─────────────────────────
                        Log.d(TAG, "TODO (Team): Query ExpenseDao.getCategoryTotalsForPeriod($uid, $startDate, $endDate)")
                        val totals = AppDatabase.getInstance(context)
                            .expenseDao()
                            .getCategoryTotalsForPeriod(uid, startDate, endDate)

                        val total = expenseList.sumOf { it.amount.toDouble() }.toFloat()
                        Log.d(TAG, "Period total: $total | Expenses: ${expenseList.size} | Categories: ${totals.size}")

                        withContext(Dispatchers.Main) {
                            isLoading      = false
                            expenses       = expenseList
                            categoryTotals = totals
                            grandTotal     = total
                            periodSummary  = "$startDate → $endDate | Total: R${"%.2f".format(total)} | ${expenseList.size} entries"
                        }
                    }
                },
                enabled  = isApplyEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor   = NavyDarkest
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = NavyDarkest,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Apply Filter")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Period summary label
            Text(periodSummary, color = LimeGreen, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(16.dp))

            // ── Use LazyColumn for the rest so we can nest both lists ──────
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // ── Section 1: Category totals ─────────────────────────────
                item {
                    Text(
                        text  = "TOTAL SPENT PER CATEGORY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (categoryTotals.isEmpty()) {
                    item {
                        // TODO (Team): Replace with real data from CategoryTotalAdapter
                        PlaceholderCard("Category totals will appear here after applying filter")
                    }
                } else {
                    // TODO (Team): Enhance with category name lookup from CategoryDao.getCategoryById
                    items(categoryTotals) { total ->
                        CategoryTotalRow(total = total)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // ── Section 2: All expense entries ─────────────────────────
                item {
                    Text(
                        text  = "ALL EXPENSE ENTRIES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (expenses.isEmpty()) {
                    item {
                        // TODO (Team): Replace with real data rows in ExpenseAdapter
                        PlaceholderCard("No expenses found for this period")
                    }
                } else {
                    items(expenses) { expense ->
                        ExpenseRow(expense = expense)
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

/** Placeholder card shown when a list has no data yet. */
@Composable
private fun PlaceholderCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Displays a single category total row in the reports list. */
@Composable
private fun CategoryTotalRow(total: CategoryTotal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // TODO (Team): Replace "Category #ID" with actual name from CategoryDao.getCategoryById
            Text(
                text  = "Category #${total.category_id ?: "Uncategorised"}",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text  = "R ${"%.2f".format(total.total)}",
                color = LimeGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Displays a single expense row with optional receipt thumbnail.
 *
 * TODO (Team): Add Coil AsyncImage to display receipt photo:
 *   if (!expense.supabaseImageUrl.isNullOrEmpty()) {
 *       AsyncImage(
 *           model              = expense.supabaseImageUrl,
 *           contentDescription = "Receipt",
 *           placeholder        = painterResource(R.drawable.ic_receipt_placeholder),
 *           error              = painterResource(R.drawable.ic_receipt_error),
 *           modifier           = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
 *       )
 *   }
 */
@Composable
private fun ExpenseRow(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO (Team): Insert Coil AsyncImage here for expense.supabaseImageUrl
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    text  = "${expense.date}  ${expense.startTime}–${expense.endTime}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
                if (!expense.supabaseImageUrl.isNullOrEmpty()) {
                    Text(
                        text  = "📎 Receipt attached",
                        color = LimeGreen,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text  = "R ${"%.2f".format(expense.amount)}",
                color = LimeGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}