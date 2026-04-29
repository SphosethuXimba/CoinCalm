package com.st10448336.coincalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.Category
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Validates and inserts new Category entities into the RoomDB. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(navController: NavController) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var categoryName  by remember { mutableStateOf("") }
    var nameError     by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }

    val isSaveEnabled = categoryName.isNotBlank() && categoryName.length <= 30 && !isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        topBar = { CoinCalmTopBar(title = "New Category", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text("Create a spending category to organise your expenses.", style = MaterialTheme.typography.bodyMedium, color = contentSecondary())
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it; nameError = if (it.length > 30) "Max 30 characters" else null },
                label = { Text("Category Name (e.g. Groceries)") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
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
                        val db = AppDatabase.getInstance(context)
                        try {
                            db.categoryDao().insertCategory(Category(userId = uid, categoryName = categoryName.trim()))
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                Toast.makeText(context, "Category created! +20 XP", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                val msg = if (e.message?.contains("UNIQUE") == true) "You already have a '${categoryName.trim()}' category" else "Failed to save. Please try again."
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    }
                },
                enabled = isSaveEnabled, modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Save Category")
            }
        }
    }
}