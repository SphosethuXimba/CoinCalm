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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.st10448336.coincalm.data.SessionPreferences
import com.st10448336.coincalm.data.UserSyncHelper
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Attribution: Firebase Authentication Login Flow
// Link: https://firebase.google.com/docs/auth/android/password-auth
// Author: Google Developers
@Composable
fun LoginScreen(navController: NavController) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHost = remember { SnackbarHostState() }
    val sessionPrefs = remember { SessionPreferences(context) }

    var email           by remember { mutableStateOf(sessionPrefs.getSavedEmail()) }
    var password        by remember { mutableStateOf("") }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var isLoading       by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe      by remember { mutableStateOf(false) }

    val emailIsValid    = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isSignInEnabled = emailIsValid && password.isNotBlank() && !isLoading

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White)
            }
        },
        containerColor = screenBackground()
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 28.dp).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(72.dp))
            Text("WELCOME BACK!", style = MaterialTheme.typography.headlineLarge, color = contentPrimary())
            Text("Sign in to CoinCalm", style = MaterialTheme.typography.bodyMedium, color = contentSecondary(), modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.replace(Regex("\\s"), ""); emailError = null },
                label = { Text("Email Address") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Password") },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = ErrorRed) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "Hide" else "Show", color = LimeGreen) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (isSignInEnabled) {
                        scope.launch {
                            performLogin(
                                context, email, password, rememberMe, sessionPrefs,
                                { emailError = it }, { passwordError = it }, { isLoading = it },
                                { navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } } },
                                { msg -> snackbarHost.showSnackbar(msg) }
                            )
                        }
                    }
                }),
                singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { navController.navigate(NavRoutes.ForgotPassword.route) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Forgot Password?", color = LimeGreen) }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe, onCheckedChange = { rememberMe = it }, enabled = !isLoading,
                    colors = CheckboxDefaults.colors(checkedColor = LimeGreen, uncheckedColor = TextHint, checkmarkColor = NavyDarkest)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Remember me on this device", color = contentPrimary(), style = MaterialTheme.typography.bodyMedium)
                    Text("Stay signed in when you reopen the app", color = contentSecondary(), style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        performLogin(
                            context, email, password, rememberMe, sessionPrefs,
                            { emailError = it }, { passwordError = it }, { isLoading = it },
                            { navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } } },
                            { msg -> snackbarHost.showSnackbar(msg) }
                        )
                    }
                },
                enabled = isSignInEnabled, modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                else Text("SIGN IN", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = { navController.navigate(NavRoutes.Register.route) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Don't have an account? ", color = contentSecondary())
                Text("Register", color = LimeGreen)
            }
        }
    }
}

private suspend fun performLogin(
    context: android.content.Context, email: String, password: String, rememberMe: Boolean, sessionPrefs: SessionPreferences,
    setEmailError: (String?) -> Unit, setPasswordError: (String?) -> Unit, setLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit, showSnackbar: suspend (String) -> Unit
) {
    val cleanEmail = email.trim().replace(Regex("\\s"), "").lowercase()
    if (cleanEmail.isBlank()) { setEmailError("Email required"); return }
    if (password.isBlank()) { setPasswordError("Password required"); return }

    setLoading(true)
    try {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()
        sessionPrefs.setRememberMe(rememberMe)
        sessionPrefs.saveEmail(cleanEmail)
        UserSyncHelper.ensureUserInRoomDb(context)
        setLoading(false)
        onSuccess()
    } catch (e: FirebaseAuthInvalidUserException) {
        setLoading(false); setEmailError("No account found")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        setLoading(false); setPasswordError("Incorrect credentials")
    } catch (e: Exception) {
        setLoading(false); showSnackbar("Login failed: ${e.message}")
    }
}