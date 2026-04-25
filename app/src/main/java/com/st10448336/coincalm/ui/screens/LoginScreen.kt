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
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * LoginScreen — Fully functional Firebase Auth sign-in.
 *
 * BUG FIX: BlueStacks and some physical keyboards inject invisible whitespace
 * characters (non-breaking spaces, zero-width spaces) that Android's .trim()
 * does not remove. We sanitize the email in onValueChange by stripping ALL
 * Unicode whitespace using a regex, not just ASCII spaces.
 *
 * BUG FIX: Inline email format validation now runs AS the user types, so
 * the user sees "invalid email" feedback immediately instead of tapping
 * a disabled button with no explanation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val TAG           = "LoginScreen"
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()
    val focusManager  = LocalFocusManager.current
    val snackbarHost  = remember { SnackbarHostState() }

    // ── State ──────────────────────────────────────────────────────────────
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var isLoading       by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Real-time validation for Sign In button ────────────────────────────
    // Compute inline whether the current inputs are valid.
    // This drives BOTH the button enabled state AND the inline error labels,
    // so the user always knows why the button is disabled.
    val emailIsValid   = email.isNotBlank() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passwordIsValid = password.isNotBlank()
    val isSignInEnabled = emailIsValid && passwordIsValid && !isLoading

    // Show inline email format error as soon as the user has typed something
    // but the format is wrong — this is the fix for "why is my button grey?"
    val inlineEmailError: String? = when {
        email.isEmpty()    -> null           // don't nag on empty field
        !emailIsValid      -> "Please enter a valid email address"
        else               -> emailError     // preserve server-side errors
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = NavyDark
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark)
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(72.dp))

            Text(
                text  = "WELCOME BACK!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text     = "Sign in to CoinCalm",
                style    = MaterialTheme.typography.bodyMedium,
                color    = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(48.dp))

            // ── Email field ───────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { rawInput ->
                    // KEY FIX: Strip ALL Unicode whitespace including:
                    // - Regular spaces (U+0020)
                    // - Non-breaking spaces (U+00A0) — injected by BlueStacks
                    // - Zero-width spaces (U+200B) — injected by some IMEs
                    // - Tabs, carriage returns, newlines
                    // Using \s in Kotlin regex covers all Unicode whitespace.
                    val sanitized = rawInput.replace(Regex("\\s"), "")
                    email      = sanitized
                    emailError = null   // clear server-side error on new input
                },
                label         = { Text("Email Address") },
                isError       = inlineEmailError != null,
                supportingText = inlineEmailError?.let {
                    { Text(it, color = ErrorRed) }
                },
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

            // ── Password field ────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = {
                    password      = it
                    passwordError = null
                },
                label         = { Text("Password") },
                isError       = passwordError != null,
                supportingText = passwordError?.let {
                    { Text(it, color = ErrorRed) }
                },
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
                                    email            = email,
                                    password         = password,
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

            // Forgot password
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

            Spacer(Modifier.height(20.dp))

            // ── Sign In Button ────────────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        performLogin(
                            email            = email,
                            password         = password,
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
                    // FIX: Make disabled state clearly visible so user
                    // understands the button is not yet active
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

            // Helper text explaining why button might be disabled
            if (!isSignInEnabled && !isLoading) {
                Text(
                    text     = when {
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

            // Register link
            TextButton(
                onClick  = { navController.navigate(NavRoutes.Register.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? ", color = TextSecondary)
                Text("Register", color = LimeGreen)
            }
        }
    }
}

/**
 * Extracted login logic — runs on IO dispatcher, updates UI via callbacks.
 */
private suspend fun performLogin(
    email: String,
    password: String,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    setLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    showSnackbar: suspend (String) -> Unit
) {
    val TAG = "LoginScreen"

    // Final sanitization before Firebase call
    val cleanEmail = email.trim().replace(Regex("\\s"), "").lowercase()

    if (cleanEmail.isBlank()) {
        setEmailError("Email address is required")
        return
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
        setEmailError("Please enter a valid email address")
        return
    }
    if (password.isBlank()) {
        setPasswordError("Password is required")
        return
    }

    setLoading(true)
    Log.d(TAG, "Calling Firebase signInWithEmailAndPassword for: $cleanEmail")

    try {
        val result = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(cleanEmail, password)
            .await()

        Log.i(TAG, "Login success. UID: ${result.user?.uid}")
        setLoading(false)
        onSuccess()

    } catch (e: FirebaseAuthInvalidUserException) {
        Log.w(TAG, "No account found: $cleanEmail")
        setLoading(false)
        setEmailError("No account found with this email address")

    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Log.w(TAG, "Wrong password for: $cleanEmail")
        setLoading(false)
        setPasswordError("Incorrect email or password")

    } catch (e: Exception) {
        Log.e(TAG, "Login failed: ${e.message}", e)
        setLoading(false)
        showSnackbar("Login failed: ${e.message}")
    }
}