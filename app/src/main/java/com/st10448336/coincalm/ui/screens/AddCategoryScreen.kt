package com.st10448336.coincalm.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Category
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
import com.st10448336.coincalm.ui.theme.screenBackground

/**
 * AddCategoryScreen — Skeleton screen for creating expense categories.
 *
 * REQUIREMENT-02: Dynamic Category Creation.
 *
 *
 * @author Sphosethu Ximba [ST10448336] — PROG7313 POE Part 2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(navController: NavController) {

    val TAG          = "AddCategoryScreen"
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var categoryName   by remember { mutableStateOf("") }
    var nameError      by remember { mutableStateOf<String?>(null) }
    var isLoading      by remember { mutableStateOf(false) }

    // Button enabled only when field has valid content (error prevention)
    val isSaveEnabled = categoryName.isNotBlank()
            && categoryName.length <= 30
            && !isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = screenBackground(),
        topBar = {
            CoinCalmTopBar(title = "New Category", onBack = { navController.popBackStack() })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text  = "Create a spending category to organise your expenses.",
                style = MaterialTheme.typography.bodyMedium,
                color = contentSecondary()
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
                label         = { Text("Category Name (e.g. Groceries)") },
                isError       = nameError != null,
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


                        // This is the actual database transaction.
                        val db          = AppDatabase.getInstance(context)
                        val newCategory = Category(userId = uid, categoryName = categoryName.trim())
                        try {
                            val rowId = db.categoryDao().insertCategory(newCategory)
                            Log.d(TAG, "Category inserted with row ID: $rowId")

                            withContext(Dispatchers.Main) {
                                isLoading = false
                                snackbarHostState.showSnackbar("'${categoryName.trim()}' created!")
                                categoryName = ""
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
    }
}