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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Category
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(navController: NavController) {

    val TAG           = "AddCategoryScreen"
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()
    val focusManager  = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var categoryName  by remember { mutableStateOf("") }
    var nameError     by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }
    var showBanner    by remember { mutableStateOf(false) }

    val isSaveEnabled = categoryName.isNotBlank()
            && categoryName.length <= 30
            && !isLoading

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDark,
        topBar = {
            CoinCalmTopBar(title = "New Category", onBack = { navController.popBackStack() })
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
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text  = "Create a spending category to organise your expenses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(28.dp))

                OutlinedTextField(
                    value         = categoryName,
                    onValueChange = {
                        categoryName = it
                        nameError = when {
                            it.length > 30 -> "Max 30 characters"
                            else           -> null
                        }
                    },
                    label          = { Text("Category Name (e.g. Groceries)") },
                    isError        = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine  = true,
                    enabled     = !isLoading,
                    colors      = coinCalmTextFieldColors(),
                    modifier    = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(28.dp))

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
                            Log.d(TAG, "Inserting category '$categoryName' for UID: $uid")

                            val db          = AppDatabase.getInstance(context)
                            val newCategory = Category(userId = uid, categoryName = categoryName.trim())

                            try {
                                val rowId = db.categoryDao().insertCategory(newCategory)
                                Log.d(TAG, "Category inserted with row ID: $rowId")

                                withContext(Dispatchers.Main) {
                                    isLoading    = false
                                    categoryName = ""

                                    // ── Show the in-app banner ──────────────
                                    showBanner = true
                                    delay(3000)
                                    showBanner = false
                                }

                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to insert category: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    val msg = if (e.message?.contains("UNIQUE") == true)
                                        "You already have a '${categoryName.trim()}' category"
                                    else "Failed to save. Please try again."
                                    snackbarHostState.showSnackbar(msg)
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
                        Text("Save Category")
                    }
                }
            }

            // ── In-app banner — floats over content at the top ──────────────
            CategorySavedBanner(visible = showBanner)
        }
    }
}

@Composable
private fun CategorySavedBanner(visible: Boolean) {
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
                    text       = "Category added successfully",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = NavyDarkest
                )
                Text(
                    text     = "Your new category has been saved successfully",
                    fontSize = 13.sp,
                    color    = NavyDark
                )
            }
        }
    }
}