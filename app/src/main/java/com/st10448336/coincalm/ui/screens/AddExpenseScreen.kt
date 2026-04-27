package com.st10448336.coincalm.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Category
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.data.repository.StorageRepository
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {

    val TAG               = "AddExpenseScreen"
    val context           = LocalContext.current
    val scope             = rememberCoroutineScope()
    val focusManager      = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var amount           by remember { mutableStateOf("") }
    var date             by remember { mutableStateOf("") }
    var startTime        by remember { mutableStateOf("") }
    var endTime          by remember { mutableStateOf("") }
    var description      by remember { mutableStateOf("") }
    var categories       by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isLoading        by remember { mutableStateOf(false) }
    var isUploading      by remember { mutableStateOf(false) }
    var photoStatus      by remember { mutableStateOf("No photo attached") }
    var supabaseImageUrl by remember { mutableStateOf<String?>(null) }
    var pendingImageUri  by remember { mutableStateOf<Uri?>(null) }
    var showBanner       by remember { mutableStateOf(false) }

    var amountError      by remember { mutableStateOf<String?>(null) }
    var dateError        by remember { mutableStateOf<String?>(null) }
    var startTimeError   by remember { mutableStateOf<String?>(null) }
    var endTimeError     by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uri = pendingImageUri
            if (uri != null) {
                isUploading = true
                photoStatus = "Uploading receipt…"
                scope.launch(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)!!
                        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                        tempFile.outputStream().use { out -> inputStream.copyTo(out) }

                        val url = StorageRepository.uploadReceipt(tempFile)
                        Log.d(TAG, "✅ Receipt uploaded: $url")

                        withContext(Dispatchers.Main) {
                            supabaseImageUrl = url
                            photoStatus      = "✅ Photo uploaded successfully"
                            isUploading      = false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Upload failed: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            photoStatus = "❌ Upload failed — expense will save without photo"
                            isUploading = false
                        }
                    }
                }
            }
        } else {
            photoStatus = "No photo taken"
        }
    }

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        date = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val list = db.categoryDao().getCategoriesForUser(uid).first()
            Log.d(TAG, "Loaded ${list.size} categories for UID: $uid")
            withContext(Dispatchers.Main) { categories = list }
        }
    }

    val isSaveEnabled = amount.isNotBlank()
            && date.isNotBlank()
            && startTime.isNotBlank()
            && endTime.isNotBlank()
            && description.isNotBlank()
            && selectedCategory != null
            && !isLoading
            && !isUploading

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDark,
        topBar = {
            CoinCalmTopBar(
                title = "Add Expense",
                onBack = {
                    navController.navigate("Dashboard") {
                        popUpTo(0) { inclusive = true }   // clears entire back stack
                        launchSingleTop = true
                    }
                }
            )
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Amount ─────────────────────────────────────────────────
                OutlinedTextField(
                    value         = amount,
                    onValueChange = {
                        amount = it
                        amountError = if (it.toFloatOrNull() == null && it.isNotEmpty())
                            "Enter a valid amount" else null
                    },
                    label          = { Text("Amount") },
                    isError        = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
                    }),
                    singleLine = true,
                    enabled    = !isLoading,
                    colors     = coinCalmTextFieldColors(),
                    modifier   = Modifier.fillMaxWidth()
                )

                // ── Date picker ────────────────────────────────────────────
                OutlinedTextField(
                    value          = date,
                    onValueChange  = {},
                    label          = { Text("Date (YYYY-MM-DD)") },
                    isError        = dateError != null,
                    supportingText = dateError?.let { { Text(it, color = ErrorRed) } },
                    readOnly       = true,
                    enabled        = !isLoading,
                    colors         = coinCalmTextFieldColors(),
                    modifier       = Modifier.fillMaxWidth(),
                    trailingIcon   = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    date = "%04d-%02d-%02d".format(y, m + 1, d)
                                    dateError = null
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { Text("Pick", color = LimeGreen) }
                    }
                )

                // ── Start / End time ───────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value         = startTime,
                        onValueChange = {},
                        label         = { Text("Start Time") },
                        isError       = startTimeError != null,
                        readOnly      = true,
                        enabled       = !isLoading,
                        colors        = coinCalmTextFieldColors(),
                        modifier      = Modifier.weight(1f),
                        trailingIcon  = {
                            TextButton(onClick = {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        startTime      = "%02d:%02d".format(h, min)
                                        startTimeError = null
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE), true
                                ).show()
                            }) { Text("Pick", color = LimeGreen) }
                        }
                    )
                    OutlinedTextField(
                        value         = endTime,
                        onValueChange = {},
                        label         = { Text("End Time") },
                        isError       = endTimeError != null,
                        readOnly      = true,
                        enabled       = !isLoading,
                        colors        = coinCalmTextFieldColors(),
                        modifier      = Modifier.weight(1f),
                        trailingIcon  = {
                            TextButton(onClick = {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        endTime      = "%02d:%02d".format(h, min)
                                        endTimeError = null
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE), true
                                ).show()
                            }) { Text("Pick", color = LimeGreen) }
                        }
                    )
                }

                // ── Description ────────────────────────────────────────────
                OutlinedTextField(
                    value         = description,
                    onValueChange = {
                        description      = it
                        descriptionError = null
                    },
                    label          = { Text("Description") },
                    isError        = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    enabled    = !isLoading,
                    colors     = coinCalmTextFieldColors(),
                    modifier   = Modifier.fillMaxWidth()
                )

                // ── Category dropdown ──────────────────────────────────────
                ExposedDropdownMenuBox(
                    expanded         = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier         = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = selectedCategory?.categoryName ?: "Select a category",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Category") },
                        trailingIcon  = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        colors   = coinCalmTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded         = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier         = Modifier.background(NavyMedium)
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text    = {
                                    Text("No categories yet — create one first", color = TextSecondary)
                                },
                                onClick = {}
                            )
                        }
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.categoryName, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    dropdownExpanded = false
                                    Log.d(TAG, "Category selected: ${cat.categoryName}")
                                }
                            )
                        }
                    }
                }

                // ── Photo section ──────────────────────────────────────────
                Text(
                    "Receipt Photo (optional)",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedButton(
                    onClick = {
                        val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        pendingImageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    enabled  = !isLoading && !isUploading,
                    border   = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color       = LimeGreen,
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading…", color = LimeGreen)
                    } else {
                        Text("📷  Take Photo", color = LimeGreen)
                    }
                }

                Text(
                    text  = photoStatus,
                    color = when {
                        photoStatus.startsWith("✅") -> LimeGreen
                        photoStatus.startsWith("❌") -> ErrorRed
                        else                         -> TextSecondary
                    },
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(Modifier.height(6.dp))

                // ── Save Expense Button ────────────────────────────────────
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid == null) {
                            scope.launch { snackbarHostState.showSnackbar("Session error — please log in again") }
                            return@Button
                        }

                        var valid = true
                        if (amount.toFloatOrNull() == null) { amountError = "Enter a valid amount"; valid = false }
                        if (date.isBlank())        { dateError        = "Date is required";        valid = false }
                        if (startTime.isBlank())   { startTimeError   = "Required";                valid = false }
                        if (endTime.isBlank())     { endTimeError     = "Required";                valid = false }
                        if (description.isBlank()) { descriptionError = "Description is required"; valid = false }
                        if (!valid) return@Button

                        isLoading = true
                        scope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(context)
                            val newExpense = Expense(
                                userId           = uid,
                                categoryId       = selectedCategory?.categoryId,
                                amount           = amount.toFloat(),
                                date             = date,
                                startTime        = startTime,
                                endTime          = endTime,
                                description      = description.trim(),
                                supabaseImageUrl = supabaseImageUrl
                            )
                            try {
                                val rowId = db.expenseDao().insertExpense(newExpense)
                                Log.d(TAG, "Expense saved. Row ID: $rowId")

                                withContext(Dispatchers.Main) {
                                    isLoading = false

                                    // ── Show the in-app banner ──────────────
                                    showBanner = true
                                    delay(3000)
                                    showBanner = false
                                    navController.popBackStack()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to save expense: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    snackbarHostState.showSnackbar("Failed to save. Please try again.")
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
                        Text("Save Expense")
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            // ── In-app banner — floats over content at the top ──────────────
            ExpenseSavedBanner(visible = showBanner)
        }
    }
}

@Composable
private fun ExpenseSavedBanner(visible: Boolean) {
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
                    text       = "Expense Saved! 💸",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = NavyDarkest
                )
                Text(
                    text     = "Your expense has been recorded successfully",
                    fontSize = 13.sp,
                    color    = NavyDark
                )
            }
<<<<<<< Updated upstream

            Text(
                text  = photoStatus,
                color = when {
                    photoStatus.startsWith("✅") -> LimeGreen
                    photoStatus.startsWith("❌") -> ErrorRed
                    else                         -> TextSecondary
                },
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(6.dp))

            // ── Save Expense Button ────────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Session error — please log in again")
                        }
                        return@Button
                    }

                    var valid = true
                    if (amount.toFloatOrNull() == null) {
                        amountError = "Enter a valid amount"; valid = false
                    }
                    if (date.isBlank())        { dateError        = "Date is required"; valid = false }
                    if (startTime.isBlank())   { startTimeError   = "Required";         valid = false }
                    if (endTime.isBlank())     { endTimeError     = "Required";         valid = false }
                    if (description.isBlank()) { descriptionError = "Description is required"; valid = false }
                    if (!valid) return@Button

                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(context)
                        val newExpense = Expense(
                            userId           = uid,
                            categoryId       = selectedCategory?.categoryId,
                            amount           = amount.toFloat(),
                            date             = date,
                            startTime        = startTime,
                            endTime          = endTime,
                            description      = description.trim(),
                            supabaseImageUrl = supabaseImageUrl
                        )
                        try {
                            val rowId = db.expenseDao().insertExpense(newExpense)
                            Log.d(TAG, "Expense saved. Row ID: $rowId | supabaseUrl: $supabaseImageUrl")
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                snackbarHostState.showSnackbar("Expense saved!")
                                navController.navigate("Dashboard") {
                                    popUpTo(0) { inclusive = true }   // clears entire stack
                                    launchSingleTop = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save expense: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                snackbarHostState.showSnackbar("Failed to save. Please try again.")
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
                    Text("Save Expense")
                }
            }

            Spacer(Modifier.height(20.dp))
=======
>>>>>>> Stashed changes
        }
    }
}