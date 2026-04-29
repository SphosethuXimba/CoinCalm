package com.st10448336.coincalm.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Category
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.data.repository.StorageRepository
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

/** Validates inputs, handles Supabase image upload, and inserts new Expense entities. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
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
    var previewExpanded  by remember { mutableStateOf(false) }

    var amountError      by remember { mutableStateOf<String?>(null) }
    var dateError        by remember { mutableStateOf<String?>(null) }
    var startTimeError   by remember { mutableStateOf<String?>(null) }
    var endTimeError     by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingImageUri != null) {
            isUploading = true
            photoStatus = "Uploading receipt…"
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(pendingImageUri!!)!!
                    val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                    tempFile.outputStream().use { out -> inputStream.copyTo(out) }

                    val url = StorageRepository.uploadReceipt(tempFile)
                    withContext(Dispatchers.Main) {
                        supabaseImageUrl = url
                        photoStatus      = "✅ Photo uploaded successfully"
                        isUploading      = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        photoStatus = "❌ Upload failed — expense will save without photo"
                        isUploading = false
                    }
                }
            }
        } else {
            photoStatus = "No photo taken"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            pendingImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            photoStatus = "❌ Camera permission denied"
        }
    }

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        date = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val list = db.categoryDao().getCategoriesForUser(uid).first()
            withContext(Dispatchers.Main) { categories = list }
        }
    }

    val isSaveEnabled = amount.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && description.isNotBlank() && selectedCategory != null && !isLoading && !isUploading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        containerColor = screenBackground(),
        topBar = { CoinCalmTopBar(title = "Add Expense", onBack = { navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Dashboard.route) { inclusive = true } } }) }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = amount, onValueChange = { amount = it; amountError = if (it.toFloatOrNull() == null && it.isNotEmpty()) "Enter a valid amount" else null },
                label = { Text("Amount") }, isError = amountError != null, supportingText = amountError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date, onValueChange = {}, label = { Text("Date (YYYY-MM-DD)") }, isError = dateError != null,
                supportingText = dateError?.let { { Text(it, color = ErrorRed) } }, readOnly = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d -> date = "%04d-%02d-%02d".format(y, m + 1, d); dateError = null }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Pick", color = LimeGreen) }
                }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startTime, onValueChange = {}, label = { Text("Start Time") }, isError = startTimeError != null, readOnly = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.weight(1f),
                    trailingIcon = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, min -> startTime = "%02d:%02d".format(h, min); startTimeError = null }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        }) { Text("Pick", color = LimeGreen) }
                    }
                )
                OutlinedTextField(
                    value = endTime, onValueChange = {}, label = { Text("End Time") }, isError = endTimeError != null, readOnly = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.weight(1f),
                    trailingIcon = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, min -> endTime = "%02d:%02d".format(h, min); endTimeError = null }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        }) { Text("Pick", color = LimeGreen) }
                    }
                )
            }

            OutlinedTextField(
                value = description, onValueChange = { description = it; descriptionError = null }, label = { Text("Description") }, isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it, color = ErrorRed) } }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = !dropdownExpanded }, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory?.categoryName ?: "Select a category", onValueChange = {}, readOnly = true, label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.background(NavyMedium)) {
                    if (categories.isEmpty()) { DropdownMenuItem(text = { Text("No categories yet — create one first", color = contentSecondary()) }, onClick = {}) }
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.categoryName, color = contentPrimary()) }, onClick = { selectedCategory = cat; dropdownExpanded = false })
                    }
                }
            }

            Text("Receipt Photo (optional)", color = contentSecondary(), style = MaterialTheme.typography.bodyMedium)

            OutlinedButton(
                onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }, enabled = !isLoading && !isUploading, border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = LimeGreen, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading…", color = LimeGreen)
                } else { Text("Take Photo", color = LimeGreen) }
            }

            Text(photoStatus, color = when { photoStatus.startsWith("✅") -> LimeGreen; photoStatus.startsWith("❌") -> ErrorRed; else -> contentSecondary() }, style = MaterialTheme.typography.labelSmall)

            if (pendingImageUri != null && !isUploading) {
                Spacer(Modifier.height(4.dp))
                Text("Receipt Preview", color = contentSecondary(), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))

                if (previewExpanded) {
                    Dialog(onDismissRequest = { previewExpanded = false }) {
                        Column(modifier = Modifier.fillMaxWidth().background(NavyDarkest, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AsyncImage(model = supabaseImageUrl ?: pendingImageUri, contentDescription = "Receipt view", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp).background(NavyDark, RoundedCornerShape(8.dp)))
                            TextButton(onClick = { previewExpanded = false }, modifier = Modifier.fillMaxWidth()) { Text("Close", color = LimeGreen) }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(NavyDark, RoundedCornerShape(10.dp)).clickable { previewExpanded = true }) {
                    AsyncImage(model = supabaseImageUrl ?: pendingImageUri, contentDescription = "Thumbnail", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(NavyDarkest.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Text("Tap to expand", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) { scope.launch { snackbarHostState.showSnackbar("Session error") }; return@Button }

                    var valid = true
                    if (amount.toFloatOrNull() == null) { amountError = "Enter valid amount"; valid = false }
                    if (date.isBlank()) { dateError = "Required"; valid = false }
                    if (startTime.isBlank()) { startTimeError = "Required"; valid = false }
                    if (endTime.isBlank()) { endTimeError = "Required"; valid = false }
                    if (description.isBlank()) { descriptionError = "Required"; valid = false }
                    if (!valid) return@Button

                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(context)
                        val newExpense = Expense(userId = uid, categoryId = selectedCategory?.categoryId, amount = amount.toFloat(), date = date, startTime = startTime, endTime = endTime, description = description.trim(), supabaseImageUrl = supabaseImageUrl)
                        try {
                            db.expenseDao().insertExpense(newExpense)
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                Toast.makeText(context, "Expense saved! +10 XP", Toast.LENGTH_SHORT).show()
                                navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Dashboard.route) { inclusive = true } }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { isLoading = false; snackbarHostState.showSnackbar("Failed to save.") }
                        }
                    }
                },
                enabled = isSaveEnabled, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint), shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(22.dp), strokeWidth = 2.dp) else Text("Save Expense")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}