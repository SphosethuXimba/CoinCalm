package com.st10448336.coincalm.ui.screens

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * LoginScreen — Firebase Auth sign-in with "Remember Me" support.
 *
 * REMEMBER ME BEHAVIOUR:
 *  - Checked  → SessionPreferences.rememberMe = true
 *               On next launch, SplashScreen skips login and goes to Dashboard.
 *  - Unchecked → SessionPreferences.rememberMe = false
 *               On next launch, SplashScreen signs out Firebase and shows Login.
 *
 * The email field is pre-filled from the last successful login
 * so the user doesn't have to retype it even when Remember Me is off.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val TAG          = "LoginScreen"
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHost = remember { SnackbarHostState() }
    val sessionPrefs = remember { SessionPreferences(context) }

    // ── State ──────────────────────────────────────────────────────────────
    // Pre-fill email from last login for convenience
    var email           by remember { mutableStateOf(sessionPrefs.getSavedEmail()) }
    var password        by remember { mutableStateOf("") }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var isLoading       by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    // Default to false — user must explicitly choose to stay logged in
    var rememberMe      by remember { mutableStateOf(false) }

    // ── Real-time validation ───────────────────────────────────────────────
    val emailIsValid    = email.isNotBlank() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passwordIsValid = password.isNotBlank()
    val isSignInEnabled = emailIsValid && passwordIsValid && !isLoading

    val inlineEmailError: String? = when {
        email.isEmpty() -> null
        !emailIsValid   -> "Please enter a valid email address"
        else            -> emailError
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = screenBackground()
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground())
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(72.dp))

            Text(
                text  = "WELCOME BACK!",
                style = MaterialTheme.typography.headlineLarge,
                color = contentPrimary()
            )
            Text(
                text     = "Sign in to CoinCalm",
                style    = MaterialTheme.typography.bodyMedium,
                color    = contentSecondary(),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(48.dp))

            // ── Email ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { rawInput ->
                    // Strip invisible whitespace injected by BlueStacks / some IMEs
                    email      = rawInput.replace(Regex("\\s"), "")
                    emailError = null
                },
                label          = { Text("Email Address") },
                isError        = inlineEmailError != null,
                supportingText = inlineEmailError?.let { { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // ── Password ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; passwordError = null },
                label         = { Text("Password") },
                isError       = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = ErrorRed) } },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text  = if (passwordVisible) "Hide" else "Show",
                            color = LimeGreen,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isSignInEnabled) {
                            scope.launch {
                                performLogin(
                                    context          = context,
                                    email            = email,
                                    password         = password,
                                    rememberMe       = rememberMe,
                                    sessionPrefs     = sessionPrefs,
                                    setEmailError    = { emailError = it },
                                    setPasswordError = { passwordError = it },
                                    setLoading       = { isLoading = it },
                                    onSuccess        = {
                                        navController.navigate(NavRoutes.Dashboard.route) {
                                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                                        }
                                    },
                                    showSnackbar = { msg -> snackbarHost.showSnackbar(msg) }
                                )
                            }
                        }
                    }
                ),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            // ── Forgot password ────────────────────────────────────────────
            TextButton(
                onClick  = {
                    if (email.isNotBlank()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                scope.launch {
                                    snackbarHost.showSnackbar("Reset email sent to $email")
                                }
                            }
                    } else {
                        emailError = "Enter your email above first"
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = LimeGreen)
            }

            Spacer(Modifier.height(8.dp))

            // ── Remember Me toggle ─────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled         = !isLoading,
                    colors          = CheckboxDefaults.colors(
                        checkedColor   = LimeGreen,
                        uncheckedColor = TextHint,
                        checkmarkColor = NavyDarkest
                    )
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text  = "Remember me on this device",
                        color = contentPrimary(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text  = "Stay signed in when you reopen the app",
                        color = contentSecondary(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Sign In Button ─────────────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        performLogin(
                            context          = context,
                            email            = email,
                            password         = password,
                            rememberMe       = rememberMe,
                            sessionPrefs     = sessionPrefs,
                            setEmailError    = { emailError = it },
                            setPasswordError = { passwordError = it },
                            setLoading       = { isLoading = it },
                            onSuccess        = {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            },
                            showSnackbar = { msg -> snackbarHost.showSnackbar(msg) }
                        )
                    }
                },
                enabled  = isSignInEnabled,
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
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text  = "SIGN IN",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Helper text explaining why button is disabled
            if (!isSignInEnabled && !isLoading) {
                Text(
                    text = when {
                        email.isBlank() && password.isBlank() ->
                            "Enter your email and password to continue"
                        email.isBlank()   -> "Email address is required"
                        !emailIsValid     -> "Please enter a valid email address"
                        password.isBlank() -> "Password is required"
                        else -> ""
                    },
                    color    = TextHint,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            TextButton(
                onClick  = { navController.navigate(NavRoutes.Register.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? ", color = contentSecondary())
                Text("Register", color = LimeGreen)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Extracted login logic.
 * Saves the Remember Me flag and email BEFORE navigating,
 * so if the app is killed mid-navigation the preference is already stored.
 */
private suspend fun performLogin(
    context: android.content.Context,
    email: String,
    password: String,
    rememberMe: Boolean,
    sessionPrefs: SessionPreferences,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    setLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    showSnackbar: suspend (String) -> Unit
) {
    val TAG = "LoginScreen"

    // Final sanitize
    val cleanEmail = email.trim().replace(Regex("\\s"), "").lowercase()

    if (cleanEmail.isBlank()) {
        setEmailError("Email address is required"); return
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
        setEmailError("Please enter a valid email address"); return
    }
    if (password.isBlank()) {
        setPasswordError("Password is required"); return
    }

    setLoading(true)
    Log.d(TAG, "Firebase signInWithEmailAndPassword: $cleanEmail | rememberMe=$rememberMe")

    try {
        val result = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(cleanEmail, password)
            .await()

        Log.i(TAG, "Login success. UID: ${result.user?.uid}")

        // Save Remember Me preference and email BEFORE navigating
        sessionPrefs.setRememberMe(rememberMe)
        sessionPrefs.saveEmail(cleanEmail)
        Log.d(TAG, "Session preference saved: rememberMe=$rememberMe")

        // Ensure RoomDB has a user row (handles new device / cloned repo)
        UserSyncHelper.ensureUserInRoomDb(context)

        setLoading(false)
        onSuccess()

    } catch (e: FirebaseAuthInvalidUserException) {
        Log.w(TAG, "No account: $cleanEmail")
        setLoading(false)
        setEmailError("No account found with this email address")

    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Log.w(TAG, "Wrong password: $cleanEmail")
        setLoading(false)
        setPasswordError("Incorrect email or password")

    } catch (e: Exception) {
        Log.e(TAG, "Login failed: ${e.message}", e)
        setLoading(false)
        showSnackbar("Login failed: ${e.message}")
    }
}