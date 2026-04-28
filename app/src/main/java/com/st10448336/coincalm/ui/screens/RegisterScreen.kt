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
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.st10448336.coincalm.data.SessionPreferences
import com.st10448336.coincalm.data.entites.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * RegisterScreen — Fully functional Firebase Auth registration.
 *
 * BUG FIX: All validation errors now show INLINE as the user types,
 * not only after tapping the button. Previously the button was silently
 * disabled with no explanation — users had no feedback about what was wrong.
 *
 * Each field now has two layers of feedback:
 *  1. Inline error label that appears as you type (real-time)
 *  2. Disabled button with a helper text explaining what still needs fixing
 *
 * BUG FIX: Email input sanitizes invisible whitespace from BlueStacks/IME.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    val TAG          = "RegisterScreen"
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHost = remember { SnackbarHostState() }

    // ── Field values ───────────────────────────────────────────────────────
    var username        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var monthlyIncome   by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("ZAR") }
    var currencyExpanded by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val currencies = listOf("ZAR", "USD", "EUR", "GBP", "BWP", "ZMW")

    // ── Inline derived error states (show as user types) ──────────────────
    // These show errors immediately so the user knows exactly what to fix,
    // rather than seeing a greyed-out button with no explanation.

    val usernameError: String? = when {
        username.isEmpty()    -> null
        username.length < 3   -> "Username must be at least 3 characters"
        else                  -> null
    }

    val emailError: String? = when {
        email.isEmpty()       -> null
        !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            -> "Please enter a valid email address"
        else                  -> null
    }

    val passwordError: String? = when {
        password.isEmpty()    -> null
        password.length < 8   -> "Password must be at least 8 characters"
        else                  -> null
    }

    val confirmPasswordError: String? = when {
        confirmPassword.isEmpty()         -> null
        confirmPassword != password       -> "Passwords do not match"
        else                              -> null
    }

    val incomeError: String? = when {
        monthlyIncome.isEmpty()           -> null
        monthlyIncome.toFloatOrNull() == null -> "Enter a valid amount (numbers only)"
        monthlyIncome.toFloat() < 0       -> "Income cannot be negative"
        else                              -> null
    }

    //  Button enabled: ALL fields filled AND all errors null
    val allFieldsFilled = username.isNotBlank()
            && email.isNotBlank()
            && password.isNotBlank()
            && confirmPassword.isNotBlank()
            && monthlyIncome.isNotBlank()

    val noErrors = usernameError == null
            && emailError == null
            && passwordError == null
            && confirmPasswordError == null
            && incomeError == null

    val isButtonEnabled = allFieldsFilled && noErrors && !isLoading

    // Human-readable hint text shown below the disabled button
    val buttonHint: String = when {
        username.isBlank()          -> "Enter a username to continue"
        usernameError != null       -> usernameError
        email.isBlank()             -> "Enter your email address"
        emailError != null          -> emailError
        password.isBlank()          -> "Enter a password (min. 8 characters)"
        passwordError != null       -> passwordError
        confirmPassword.isBlank()   -> "Confirm your password"
        confirmPasswordError != null -> confirmPasswordError
        monthlyIncome.isBlank()     -> "Enter your monthly income"
        incomeError != null         -> incomeError
        else                        -> ""
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text  = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text     = "Start your financial journey",
                style    = MaterialTheme.typography.bodyMedium,
                color    = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // ── Username ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = username,
                onValueChange = { username = it },
                label         = { Text("Username") },
                // Show error ONLY after user has started typing something
                isError       = usernameError != null,
                supportingText = usernameError?.let {
                    { Text(it, color = ErrorRed) }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // ── Email ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { rawInput ->
                    // Sanitize: remove all whitespace including BlueStacks
                    // invisible characters (non-breaking spaces, zero-width spaces)
                    email = rawInput.replace(Regex("\\s"), "")
                },
                label         = { Text("Email Address") },
                isError       = emailError != null,
                supportingText = emailError?.let {
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

            //  Password
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password (min. 8 characters)") },
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
                            color = LimeGreen
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
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

            // Confirm Password
            OutlinedTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = { Text("Confirm Password") },
                isError       = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let {
                    { Text(it, color = ErrorRed) }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
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

            //  Monthly Income
            OutlinedTextField(
                value         = monthlyIncome,
                onValueChange = { monthlyIncome = it },
                label         = { Text("Monthly Income") },
                isError       = incomeError != null,
                supportingText = incomeError?.let {
                    { Text(it, color = ErrorRed) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                enabled    = !isLoading,
                colors     = coinCalmTextFieldColors(),
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            //  Currency Dropdown
            Text(
                text  = "Currency",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded         = currencyExpanded,
                onExpandedChange = { currencyExpanded = !currencyExpanded },
                modifier         = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value         = selectedCurrency,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Select Currency") },
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = currencyExpanded
                        )
                    },
                    colors   = coinCalmTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    modifier         = Modifier.background(NavyMedium)
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text    = { Text(currency, color = TextPrimary) },
                            onClick = {
                                selectedCurrency = currency
                                currencyExpanded = false
                                Log.d(TAG, "Currency selected: $currency")
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            var rememberMe by remember { mutableStateOf(true) } // default true for new registrations

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
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text  = "Stay signed in when you reopen the app",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // ── Create Account Button ──────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val cleanEmail = email.trim()
                        .replace(Regex("\\s"), "")
                        .lowercase()
                    val income = monthlyIncome.toFloatOrNull() ?: 0f

                    scope.launch {
                        performRegistration(
                            context       = context,
                            username      = username.trim(),
                            email         = cleanEmail,
                            password      = password,
                            monthlyIncome = income,
                            currency      = selectedCurrency,
                            rememberMe = rememberMe,
                                    setLoading    = { isLoading = it },
                            onSuccess     = {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Register.route) { inclusive = true }
                                }
                            },
                            showSnackbar  = { msg -> snackbarHost.showSnackbar(msg) }
                        )
                    }
                },
                enabled  = isButtonEnabled,
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
                        text  = "Create My Account",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (!isButtonEnabled && !isLoading && buttonHint.isNotBlank()) {
                Text(
                    text     = "⚠ $buttonHint",
                    color    = WarningAmber,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick  = { navController.navigate(NavRoutes.Login.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? ", color = TextSecondary)
                Text("Sign In", color = LimeGreen)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Firebase Auth + RoomDB registration logic extracted from composable. */
private suspend fun performRegistration(
    context: android.content.Context,
    username: String,
    email: String,
    password: String,
    monthlyIncome: Float,
    currency: String,
    rememberMe: Boolean,          // ADD
    setLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    showSnackbar: suspend (String) -> Unit
) {
    val TAG = "RegisterScreen"
    setLoading(true)
    Log.d(TAG, "Calling Firebase createUserWithEmailAndPassword for: $email")

    try {
        // Step 1: Firebase Auth
        val authResult = FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .await()

        val uid = authResult.user?.uid
            ?: throw Exception("Firebase returned null UID after registration")

        Log.i(TAG, "Firebase registration success. UUID: $uid")

        // Step 2: Save profile to local RoomDB
        withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).userDao().insertUser(
                User(
                    firebaseUuid = uid,
                    username = username,
                    email = email,
                    monthlyIncome = monthlyIncome,
                    currencyPreference = currency
                )
            )
            Log.d(TAG, "User saved to RoomDB. UUID: $uid")

            // Save session preference — registration implies the user
            // wants to stay on this device since it's their first time
            val sessionPrefs = SessionPreferences(context)
            sessionPrefs.setRememberMe(rememberMe)
            sessionPrefs.saveEmail(email)
            Log.d(TAG, "Session saved after registration: rememberMe=$rememberMe")
        }

        setLoading(false)
        onSuccess()

    } catch (e: FirebaseAuthWeakPasswordException) {
        Log.w(TAG, "Weak password: ${e.message}")
        setLoading(false)
        showSnackbar("Password is too weak — use at least 8 characters with numbers")

    } catch (e: FirebaseAuthUserCollisionException) {
        Log.w(TAG, "Email already registered: $email")
        setLoading(false)
        showSnackbar("An account with this email already exists. Try logging in instead.")

    } catch (e: Exception) {
        Log.e(TAG, "Registration failed: ${e.message}", e)
        setLoading(false)
        showSnackbar("Registration failed: ${e.message}")
    }
}