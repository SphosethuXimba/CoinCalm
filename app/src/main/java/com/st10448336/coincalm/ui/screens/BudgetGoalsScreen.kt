package com.st10448336.coincalm.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showBanner  by remember { mutableStateOf(false) }

    val currentMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val goal = db.goalDao().getGoalForMonth(uid, currentMonth)
            Log.d(TAG, "Existing goal for $currentMonth: $goal")
            withContext(Dispatchers.Main) {
                if (goal != null) {
                    minGoalStr = goal.minGoalAmount.toString()
                    maxGoalStr = goal.maxGoalAmount.toString()
                    goalStatus = "Current goal: R${goal.minGoalAmount} – R${goal.maxGoalAmount}"
                } else {
                    goalStatus = "No goal set for $currentMonth yet"
                }
            }
        }
    }

    val minFloat = minGoalStr.toFloatOrNull()
    val maxFloat = maxGoalStr.toFloatOrNull()

    val bothFieldsValid = minFloat != null && minFloat >= 0 &&
            maxFloat != null && maxFloat >= 0

    val maxGreaterThanMin = if (bothFieldsValid) maxFloat!! > minFloat!! else false
    val isSaveEnabled = bothFieldsValid && maxGreaterThanMin && !isLoading

    val maxConstraintError: String? = when {
        maxGoalStr.isBlank() || minGoalStr.isBlank() -> null
        !bothFieldsValid -> null
        !maxGreaterThanMin -> "Maximum must be greater than minimum (R$minFloat)"
        else -> null
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDark,
        topBar = {
            CoinCalmTopBar(title = "Budget Goals", onBack = { navController.popBackStack() })
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text(
                    text  = "Set your monthly spending band. Max must be greater than min.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Text(goalStatus, color = LimeGreen)

                OutlinedTextField(
                    value = minGoalStr,
                    onValueChange = {
                        minGoalStr = it
                        minError = if (it.toFloatOrNull() == null && it.isNotEmpty())
                            "Enter a valid amount" else null
                    },
                    label = { Text("Minimum Goal Amount") },
                    isError = minError != null,
                    supportingText = minError?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    ),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = coinCalmTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxGoalStr,
                    onValueChange = {
                        maxGoalStr = it
                        maxError = if (it.toFloatOrNull() == null && it.isNotEmpty())
                            "Enter a valid amount" else null
                    },
                    label = { Text("Maximum Goal Amount") },
                    isError = maxError != null || maxConstraintError != null,
                    supportingText = (maxConstraintError ?: maxError)?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = coinCalmTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

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

                            val db = AppDatabase.getInstance(context)
                            val newGoal = Goal(
                                userId        = uid,
                                targetMonth   = currentMonth,
                                minGoalAmount = minFloat!!,
                                maxGoalAmount = maxFloat!!
                            )

                            try {
                                db.goalDao().insertGoal(newGoal)

                                withContext(Dispatchers.Main) {
                                    isLoading  = false
                                    goalStatus = "Saved: R$minFloat – R$maxFloat for $currentMonth"

                                    // ── Show the in-app banner ──────────────────
                                    showBanner = true
                                    delay(3000)
                                    showBanner = false
                                }

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    snackbarHostState.showSnackbar("Failed to save goal. Try again.")
                                }
                            }
                        }
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = LimeGreen,
                        contentColor           = NavyDarkest,
                        disabledContainerColor = NavyLight,
                        disabledContentColor   = TextHint
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color    = NavyDarkest,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("Save Goal")
                    }
                }
            }

            // ── In-app banner — floats over content at the top ──────────────
            GoalSavedBanner(visible = showBanner)
        }
    }
}

@Composable
private fun GoalSavedBanner(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LimeGreen)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text       = "New Goal! 🎯",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = NavyDarkest
                )
                Text(
                    text     = "Your goal has been added successfully",
                    fontSize = 13.sp,
                    color    = NavyDark
                )
            }
        }
    }
}