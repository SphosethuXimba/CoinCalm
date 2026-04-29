package com.st10448336.coincalm.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.dao.CategoryTotal
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/** Implements dynamic period-based filtering for expenses and category totals. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark  = LocalDarkMode.current

    var startDate      by remember { mutableStateOf("") }
    var endDate        by remember { mutableStateOf("") }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var isLoading      by remember { mutableStateOf(false) }
    var expenses       by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var categoryTotals by remember { mutableStateOf<List<CategoryTotal>>(emptyList()) }
    var grandTotal     by remember { mutableStateOf(0f) }
    var periodSummary  by remember { mutableStateOf("Select a date range and tap Apply") }

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        val today = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        startDate = firstDay
        endDate   = today
    }

    val isApplyEnabled = startDate.isNotBlank() && endDate.isNotBlank() && !isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        containerColor = screenBackground(),
        topBar = { CoinCalmTopBar(title = "Reports & History", onBack = { navController.popBackStack() }) }
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("SELECT DATE RANGE", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp)
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startDate, onValueChange = {}, label = { Text("Start Date") }, isError = startDateError != null, readOnly = true, colors = coinCalmTextFieldColors(), modifier = Modifier.weight(1f),
                    trailingIcon = { TextButton(onClick = { val cal = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> startDate = "%04d-%02d-%02d".format(y, m + 1, d); startDateError = null }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show() }) { Text("Pick", color = LimeGreen) } }
                )
                OutlinedTextField(
                    value = endDate, onValueChange = {}, label = { Text("End Date") }, readOnly = true, colors = coinCalmTextFieldColors(), modifier = Modifier.weight(1f),
                    trailingIcon = { TextButton(onClick = { val cal = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> endDate = "%04d-%02d-%02d".format(y, m + 1, d) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show() }) { Text("Pick", color = LimeGreen) } }
                )
            }
            startDateError?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (startDate > endDate) { startDateError = "Start date cannot be after end date"; return@Button }
                    startDateError = null
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) { scope.launch { snackbarHostState.showSnackbar("Session error") }; return@Button }

                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        val expenseList = AppDatabase.getInstance(context).expenseDao().getExpensesForPeriod(uid, startDate, endDate)
                        val totals = AppDatabase.getInstance(context).expenseDao().getCategoryTotalsForPeriod(uid, startDate, endDate)
                        val total = expenseList.sumOf { it.amount.toDouble() }.toFloat()

                        withContext(Dispatchers.Main) {
                            isLoading = false
                            expenses = expenseList
                            categoryTotals = totals
                            grandTotal = total
                            periodSummary = "$startDate → $endDate | Total: R${"%.2f".format(total)} | ${expenseList.size} entries"
                        }
                    }
                },
                enabled = isApplyEnabled, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest), shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Apply Filter")
            }

            Spacer(Modifier.height(12.dp))
            Text(periodSummary, color = LimeGreen, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { Text("TOTAL SPENT PER CATEGORY", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp); Spacer(Modifier.height(8.dp)) }
                if (categoryTotals.isEmpty()) { item { PlaceholderCard("Category totals will appear here", isDark) } } else { items(categoryTotals) { total -> CategoryTotalRow(total, isDark) } }
                item { Spacer(Modifier.height(24.dp)) }
                item { Text("ALL EXPENSE ENTRIES", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp); Spacer(Modifier.height(8.dp)) }
                if (expenses.isEmpty()) { item { PlaceholderCard("No expenses found for this period", isDark) } } else { items(expenses) { expense -> ExpenseRow(expense, isDark) } }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun PlaceholderCard(text: String, isDark: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(text, color = contentSecondary(), style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun CategoryTotalRow(total: CategoryTotal, isDark: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Category #${total.category_id ?: "Uncategorised"}", color = contentPrimary(), style = MaterialTheme.typography.bodyMedium)
            Text("R ${"%.2f".format(total.total)}", color = LimeGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, isDark: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, color = contentPrimary(), fontWeight = FontWeight.SemiBold)
                Text("${expense.date}  ${expense.startTime}–${expense.endTime}", color = contentSecondary(), style = MaterialTheme.typography.labelSmall)
                if (!expense.supabaseImageUrl.isNullOrEmpty()) { Text("Receipt attached", color = LimeGreen, style = MaterialTheme.typography.labelSmall) }
            }
            Text("R ${"%.2f".format(expense.amount)}", color = LimeGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}