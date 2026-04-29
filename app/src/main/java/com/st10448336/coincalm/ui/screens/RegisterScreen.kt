package com.st10448336.coincalm.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.SessionPreferences
import com.st10448336.coincalm.data.entites.User
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Attribution: Firebase Authentication Registration Flow
// Link: https://firebase.google.com/docs/auth/android/password-auth
// Author: Google Developers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHost = remember { SnackbarHostState() }

    var username         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var monthlyIncome    by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(false) }
    var passwordVisible  by remember { mutableStateOf(false) }
    var rememberMe       by remember { mutableStateOf(true) }

    val usernameError = if (username.isNotEmpty() && username.length < 3) "Min 3 characters" else null
    val emailError = if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email" else null
    val passwordError = if (password.isNotEmpty() && password.length < 8) "Min 8 characters" else null
    val confirmError = if (confirmPassword.isNotEmpty() && confirmPassword != password) "Passwords do not match" else null
    val incomeError = if (monthlyIncome.isNotEmpty() && (monthlyIncome.toFloatOrNull() == null || monthlyIncome.toFloat() < 0)) "Invalid amount" else null

    val isButtonEnabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && monthlyIncome.isNotBlank() &&
            usernameError == null && emailError == null && passwordError == null && confirmError == null && incomeError == null && !isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(NavyDark).padding(padding).padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Text("Start your financial journey", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 14.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it }, label = { Text("Username") },
                isError = usernameError != null, supportingText = usernameError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it.replace(Regex("\\s"), "") }, label = { Text("Email Address") },
                isError = emailError != null, supportingText = emailError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("Password") },
                isError = passwordError != null, supportingText = passwordError?.let { { Text(it, color = ErrorRed) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "Hide" else "Show", color = LimeGreen) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") },
                isError = confirmError != null, supportingText = confirmError?.let { { Text(it, color = ErrorRed) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = monthlyIncome, onValueChange = { monthlyIncome = it }, label = { Text("Monthly Income") },
                isError = incomeError != null, supportingText = incomeError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe, onCheckedChange = { rememberMe = it }, enabled = !isLoading,
                    colors = CheckboxDefaults.colors(checkedColor = LimeGreen, uncheckedColor = TextHint, checkmarkColor = NavyDarkest)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Remember me on this device", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        performRegistration(
                            context, username, email.replace(Regex("\\s"), "").lowercase(), password, monthlyIncome.toFloatOrNull() ?: 0f,
                            "ZAR", rememberMe, { isLoading = it },
                            { navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Register.route) { inclusive = true } } },
                            { msg -> snackbarHost.showSnackbar(msg) }
                        )
                    }
                },
                enabled = isButtonEnabled, modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                else Text("Create My Account", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(NavRoutes.Login.route) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Already have an account? ", color = TextSecondary)
                Text("Sign In", color = LimeGreen)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private suspend fun performRegistration(
    context: android.content.Context, username: String, email: String, password: String, monthlyIncome: Float, currency: String, rememberMe: Boolean,
    setLoading: (Boolean) -> Unit, onSuccess: () -> Unit, showSnackbar: suspend (String) -> Unit
) {
    setLoading(true)
    try {
        val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Firebase UUID null")

        withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).userDao().insertUser(User(uid, username, email, monthlyIncome, currency))
            val sessionPrefs = SessionPreferences(context)
            sessionPrefs.setRememberMe(rememberMe)
            sessionPrefs.saveEmail(email)
        }
        setLoading(false)
        onSuccess()
    } catch (e: FirebaseAuthWeakPasswordException) {
        setLoading(false); showSnackbar("Password too weak")
    } catch (e: FirebaseAuthUserCollisionException) {
        setLoading(false); showSnackbar("Account already exists")
    } catch (e: Exception) {
        setLoading(false); showSnackbar("Registration failed: ${e.message}")
    }
}