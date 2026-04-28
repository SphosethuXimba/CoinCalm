package com.st10448336.coincalm.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Goal
import com.st10448336.coincalm.ui.theme.ErrorRed
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDark
import com.st10448336.coincalm.ui.theme.NavyDarkest
import com.st10448336.coincalm.ui.theme.NavyLight
import com.st10448336.coincalm.ui.theme.TextHint
import com.st10448336.coincalm.ui.theme.TextSecondary
import com.st10448336.coincalm.ui.theme.coinCalmTextFieldColors
import com.st10448336.coincalm.ui.theme.contentSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.st10448336.coincalm.ui.theme.screenBackground

/**
 * BudgetGoalsScreen — Set monthly min/max spending band.
 *
 * REQUIREMENT-04: Min/Max Budget Goal Configuration.
 */
@Composable
fun BudgetGoalsScreen(navController: NavController) {

    val TAG          = "BudgetGoalsScreen"
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var minGoalStr  by remember { mutableStateOf("") }
    var maxGoalStr  by remember { mutableStateOf("") }
    var minError    by remember { mutableStateOf<String?>(null) }
    var maxError    by remember { mutableStateOf<String?>(null) }
    var isLoading   by remember { mutableStateOf(false) }
    var goalStatus  by remember { mutableStateOf("Loading current goal...") }

    val currentMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    // ── Load existing goal for pre-filling ─────────────────────────────────
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val goal = db.goalDao().getGoalForMonth(uid, currentMonth)
            Log.d(TAG, "Existing goal for $currentMonth: $goal")
            withContext(Dispatchers.Main) {
                if (goal != null) {
                    minGoalStr  = goal.minGoalAmount.toString()
                    maxGoalStr  = goal.maxGoalAmount.toString()
                    goalStatus  = "Current goal: R${goal.minGoalAmount} – R${goal.maxGoalAmount}"
                } else {
                    goalStatus = "No goal set for $currentMonth yet"
                }
            }
        }
    }

    // ── Real-time validation for Save button (Compose State) ───────────────
    val minFloat = minGoalStr.toFloatOrNull()
    val maxFloat = maxGoalStr.toFloatOrNull()

    // Rule 1 & 2: both must be non-empty valid positive numbers
    val bothFieldsValid = minFloat != null && minFloat >= 0
            && maxFloat != null && maxFloat >= 0

    // Rule 3: CRITICAL — max MUST be strictly greater than min
    val maxGreaterThanMin = if (bothFieldsValid) maxFloat!! > minFloat!! else false

    val isSaveEnabled = bothFieldsValid && maxGreaterThanMin && !isLoading

    // Show the constraint error inline as the user types
    val maxConstraintError: String? = when {
        maxGoalStr.isBlank() || minGoalStr.isBlank() -> null
        !bothFieldsValid -> null
        !maxGreaterThanMin -> "Maximum must be greater than minimum (R$minFloat)"
        else -> null
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = screenBackground(),
        topBar = {
            CoinCalmTopBar(title = "Budget Goals", onBack = { navController.popBackStack() })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text  = "Set your monthly spending band. Max must be greater than min.",
                style = MaterialTheme.typography.bodyMedium,
                color = contentSecondary()
            )

            // Current goal status label
            Text(goalStatus, color = LimeGreen, style = MaterialTheme.typography.bodyMedium)

            // ── Minimum Goal ───────────────────────────────────────────────
            OutlinedTextField(
                value         = minGoalStr,
                onValueChange = {
                    minGoalStr = it
                    minError   = if (it.toFloatOrNull() == null && it.isNotEmpty())
                        "Enter a valid amount" else null
                },
                label         = { Text("Minimum Goal Amount") },
                isError       = minError != null,
                supportingText = minError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                ),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            // ── Maximum Goal ───────────────────────────────────────────────
            OutlinedTextField(
                value         = maxGoalStr,
                onValueChange = {
                    maxGoalStr = it
                    maxError   = if (it.toFloatOrNull() == null && it.isNotEmpty())
                        "Enter a valid amount" else null
                },
                label         = { Text("Maximum Goal Amount") },
                isError       = maxError != null || maxConstraintError != null,
                supportingText = (maxConstraintError ?: maxError)?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            // ── Save Button ────────────────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        scope.launch { snackbarHostState.showSnackbar("Session error — please log in again") }
                        return@Button
                    }

                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        Log.d(TAG, "Saving goal for $currentMonth: min=$minFloat, max=$maxFloat")

                        val db = AppDatabase.getInstance(context)
                        val newGoal = Goal(
                            userId = uid,
                            targetMonth = currentMonth,
                            minGoalAmount = minFloat!!,
                            maxGoalAmount = maxFloat!!
                        )
                        try {
                            val rowId = db.goalDao().insertGoal(newGoal)
                            Log.d(TAG, "Goal saved/replaced. Row ID: $rowId for month: $currentMonth")
                            withContext(Dispatchers.Main) {
                                isLoading  = false
                                goalStatus = "Saved: R$minFloat – R$maxFloat for $currentMonth"
                                snackbarHostState.showSnackbar("Budget goal saved!")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save goal: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                snackbarHostState.showSnackbar("Failed to save goal. Try again.")
                            }
                        }
                    }
                },
                enabled  = isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = LimeGreen,
                    contentColor           = NavyDarkest,
                    disabledContainerColor = NavyLight,
                    disabledContentColor   = TextHint
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = NavyDarkest,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Goal")
                }
            }
        }
    }
}