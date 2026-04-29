package com.st10448336.coincalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Goal
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Handles the creation and updating of the Global Monthly Budget limits. */
@Composable
fun BudgetGoalsScreen(navController: NavController) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark       = LocalDarkMode.current

    var minGoalStr     by remember { mutableStateOf("") }
    var maxGoalStr     by remember { mutableStateOf("") }
    var minError       by remember { mutableStateOf<String?>(null) }
    var maxError       by remember { mutableStateOf<String?>(null) }
    var isLoading      by remember { mutableStateOf(false) }

    var currentMinGoal by remember { mutableStateOf<Float?>(null) }
    var currentMaxGoal by remember { mutableStateOf<Float?>(null) }

    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val goal = db.goalDao().getGoalForMonth(uid, currentMonth)
            withContext(Dispatchers.Main) {
                if (goal != null) {
                    minGoalStr     = goal.minGoalAmount.toString()
                    maxGoalStr     = goal.maxGoalAmount.toString()
                    currentMinGoal = goal.minGoalAmount
                    currentMaxGoal = goal.maxGoalAmount
                }
            }
        }
    }

    val minFloat = minGoalStr.toFloatOrNull()
    val maxFloat = maxGoalStr.toFloatOrNull()

    val bothFieldsValid = minFloat != null && minFloat >= 0 && maxFloat != null && maxFloat >= 0
    val maxGreaterThanMin = if (bothFieldsValid) maxFloat!! > minFloat!! else false
    val isSaveEnabled = bothFieldsValid && maxGreaterThanMin && !isLoading

    val maxConstraintError: String? = when {
        maxGoalStr.isBlank() || minGoalStr.isBlank() -> null
        !bothFieldsValid -> null
        !maxGreaterThanMin -> "Maximum must be greater than minimum (R$minFloat)"
        else -> null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        topBar = { CoinCalmTopBar(title = "Monthly Budget Goal", onBack = { navController.popBackStack() }) }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (currentMinGoal != null && currentMaxGoal != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Active Target for $currentMonth", color = contentSecondary(), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Minimum", color = contentSecondary(), fontSize = 12.sp)
                                Text("R$currentMinGoal", color = LimeGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Maximum", color = contentSecondary(), fontSize = 12.sp)
                                Text("R$currentMaxGoal", color = ErrorRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No goal set for $currentMonth yet.", color = contentSecondary())
                    }
                }
            }

            HorizontalDivider(color = contentSecondary().copy(alpha = 0.2f))

            Text("Set or update your global spending limits for the current month.", style = MaterialTheme.typography.bodyMedium, color = contentSecondary())

            OutlinedTextField(
                value = minGoalStr, onValueChange = { minGoalStr = it; minError = if (it.toFloatOrNull() == null && it.isNotEmpty()) "Enter a valid amount" else null },
                label = { Text("Minimum Spend Target") }, isError = minError != null, supportingText = minError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = maxGoalStr, onValueChange = { maxGoalStr = it; maxError = if (it.toFloatOrNull() == null && it.isNotEmpty()) "Enter a valid amount" else null },
                label = { Text("Maximum Spend Limit") }, isError = maxError != null || maxConstraintError != null, supportingText = (maxConstraintError ?: maxError)?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) { scope.launch { snackbarHostState.showSnackbar("Session error") }; return@Button }

                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(context)
                        try {
                            val existingGoal = db.goalDao().getGoalForMonth(uid, currentMonth)

                            if (existingGoal != null) {
                                val updatedGoal = existingGoal.copy(minGoalAmount = minFloat!!, maxGoalAmount = maxFloat!!)
                                db.goalDao().updateGoal(updatedGoal)
                            } else {
                                val newGoal = Goal(userId = uid, targetMonth = currentMonth, minGoalAmount = minFloat!!, maxGoalAmount = maxFloat!!)
                                db.goalDao().insertGoal(newGoal)
                            }

                            withContext(Dispatchers.Main) {
                                isLoading = false
                                Toast.makeText(context, "Budget goal saved! +30 XP", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { isLoading = false; snackbarHostState.showSnackbar("Failed to save.") }
                        }
                    }
                },
                enabled  = isSaveEnabled, modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(22.dp), strokeWidth = 2.dp) else Text("Save Goal")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}