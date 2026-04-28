package com.st10448336.coincalm.ui.screens

import android.util.Log
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
import androidx.compose.ui.draw.clip
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
import kotlin.math.roundToInt

// Data holders for summary cards

/**
 * Represents a single category's spending summary for the current month.
 * Populates this from ExpenseDao (e.g. getTotalSpentByCategory(uid, month)).
 */
data class CategorySpendSummary(
    val categoryName: String,
    val spent: Float,
    val maxAmount: Float
)

/**
 * Represents a single savings target and how much has been saved toward it.
 * Populates this from SavingsTargetDao or equivalent.
 */
data class SavingsTargetSummary(
    val targetName: String,
    val saved: Float,
    val targetAmount: Float
)

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

    // ── State for the new summary cards ────────────────────────────────────
    var categorySpends  by remember { mutableStateOf<List<CategorySpendSummary>>(emptyList()) }
    var savingsTargets  by remember { mutableStateOf<List<SavingsTargetSummary>>(emptyList()) }

    val currentMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    // Load existing goal for pre-filling
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

    //  Load category spends and savings targets
    //         val targets = db.savingsTargetDao().getTargetsForUser(uid)
    //         savingsTargets = targets.map { SavingsTargetSummary(it.name, it.savedAmount, it.targetAmount) }
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)

                // Category Spend

                val mapped: List<CategorySpendSummary> = listOf(
                    CategorySpendSummary("Groceries",     850f,  1000f),
                    CategorySpendSummary("Transport",     420f,   400f),
                    CategorySpendSummary("Dining Out",    230f,   500f),
                    CategorySpendSummary("Entertainment", 310f,   300f)
                )

                // Savings Targets
                val targets: List<SavingsTargetSummary> = listOf(
                    SavingsTargetSummary("Emergency Fund", 4500f, 10000f),
                    SavingsTargetSummary("Holiday",        2800f,  3000f),
                    SavingsTargetSummary("New Laptop",      800f,  2000f)
                )

                withContext(Dispatchers.Main) {
                    categorySpends = mapped
                    savingsTargets = targets
                    Log.d(TAG, "Loaded ${mapped.size} category spends, ${targets.size} savings targets")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load summary cards: ${e.message}", e)
            }
        }
    }

    // Real-time validation for Save button (Compose State)
    val minFloat = minGoalStr.toFloatOrNull()
    val maxFloat = maxGoalStr.toFloatOrNull()

    // Rule 1 & 2: both must be non-empty valid positive numbers
    val bothFieldsValid = minFloat != null && minFloat >= 0
            && maxFloat != null && maxFloat >= 0

    // Rule 3: CRITICAL — max MUST be strictly greater than min
    val maxGreaterThanMin = if (bothFieldsValid) maxFloat!! > minFloat!! else false

    val isSaveEnabled = bothFieldsValid && maxGreaterThanMin && !isLoading

    // Shows the constraint error inline as the user types
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text  = "Set your monthly spending band. Max must be greater than min.",
                style = MaterialTheme.typography.bodyMedium,
                color = contentSecondary()
            )

            // Current goal status label
            Text(goalStatus, color = LimeGreen, style = MaterialTheme.typography.bodyMedium)

            //  Minimum Goal
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

            // Maximum Goal
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

            // Save Button
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


            //  CATEGORY SPENDING CARDS

            if (categorySpends.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text       = "Category Spending",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )

                categorySpends.forEach { summary ->
                    CategorySpendCard(summary = summary)
                }
            }

            //  SAVINGS TARGET CARDS

            if (savingsTargets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text       = "Savings Targets",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )

                savingsTargets.forEach { target ->
                    SavingsTargetCard(target = target)
                }
            }

            // Bottom spacing so the last card isn't flush with the screen edge
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


//  CATEGORY SPEND CARD
//  Shows: category name · spent vs max · progress bar · "On Track" / "Over!" tag


@Composable
private fun CategorySpendCard(summary: CategorySpendSummary) {

    // Clamp to [0, 1] so the bar never overflows visually
    val rawProgress  = if (summary.maxAmount > 0f) summary.spent / summary.maxAmount else 0f
    val barProgress  = rawProgress.coerceIn(0f, 1f)
    val isOver       = summary.spent > summary.maxAmount
    val percentLabel = "${(rawProgress * 100).roundToInt()}%"

    // Colour logic: green when safe, red when over
    val trackColour  = if (isOver) ErrorRed else LimeGreen
    val tagBg        = if (isOver) ErrorRed.copy(alpha = 0.18f) else LimeGreen.copy(alpha = 0.15f)
    val tagText      = if (isOver) "Over!" else "On Track"
    val tagTextColor = if (isOver) ErrorRed else LimeGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: category name + status tag
            Row(
                modifier       = Modifier.fillMaxWidth(),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = summary.categoryName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )

                // Status tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(tagBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = tagText,
                        color      = tagTextColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 2: spent / max amounts + percentage
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                // Spent amount
                Column {
                    Text(
                        text  = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text       = "R${"%.2f".format(summary.spent)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (isOver) ErrorRed else Color.White
                    )
                }

                // Percentage label in the middle
                Text(
                    text      = percentLabel,
                    fontSize  = 13.sp,
                    color     = trackColour,
                    fontWeight = FontWeight.SemiBold
                )

                // Max amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text  = "Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text       = "R${"%.2f".format(summary.maxAmount)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }

            // Row 3: progress bar
            LinearProgressIndicator(
                progress           = { barProgress },
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color              = trackColour,
                trackColor         = NavyLight,
                strokeCap          = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}


//  SAVINGS TARGET CARD
//  Shows: target name · saved vs target amount · % badge · progress bar


@Composable
private fun SavingsTargetCard(target: SavingsTargetSummary) {

    val rawProgress  = if (target.targetAmount > 0f) target.saved / target.targetAmount else 0f
    val barProgress  = rawProgress.coerceIn(0f, 1f)
    val percentInt   = (rawProgress * 100).roundToInt()
    val isComplete   = target.saved >= target.targetAmount

    // Badge colour: gold/lime when complete, soft green otherwise
    val badgeColor   = if (isComplete) LimeGreen else LimeGreen.copy(alpha = 0.75f)
    val badgeBg      = LimeGreen.copy(alpha = 0.15f)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: target name + percentage badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = target.targetName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )

                // Percentage badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = if (isComplete) "✓ Done" else "$percentInt%",
                        color      = badgeColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 2: saved / target amounts
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                // Saved amount
                Column {
                    Text(
                        text  = "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text       = "R${"%.2f".format(target.saved)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = LimeGreen
                    )
                }

                // Remaining label
                val remaining = (target.targetAmount - target.saved).coerceAtLeast(0f)
                if (!isComplete) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "Remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text       = "R${"%.2f".format(remaining)}",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color      = TextSecondary
                        )
                    }
                }

                // Target amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text  = "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text       = "R${"%.2f".format(target.targetAmount)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }

            // Row 3: progress bar
            LinearProgressIndicator(
                progress   = { barProgress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color      = if (isComplete) LimeGreen else LimeGreen.copy(alpha = 0.8f),
                trackColor = NavyLight,
                strokeCap  = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}