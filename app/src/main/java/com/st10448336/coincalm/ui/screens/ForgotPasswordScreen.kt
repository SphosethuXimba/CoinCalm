package com.st10448336.coincalm.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Attribution: Firebase Password Reset Implementation
// Link: https://firebase.google.com/docs/auth/android/manage-users#send_a_password_reset_email
// Author: Google Developers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHost = remember { SnackbarHostState() }

    var email       by remember { mutableStateOf("") }
    var emailError  by remember { mutableStateOf<String?>(null) }
    var isLoading   by remember { mutableStateOf(false) }
    var emailSent   by remember { mutableStateOf(false) }
    var sentToEmail by remember { mutableStateOf("") }

    val emailIsValid  = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isSendEnabled = emailIsValid && !isLoading
    val inlineEmailError = if (email.isNotEmpty() && !emailIsValid) "Invalid email address" else emailError

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) { data -> Snackbar(snackbarData = data, containerColor = NavyDarkest, contentColor = Color.White) } },
        containerColor = screenBackground(),
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", color = contentPrimary()) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LimeGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBackground())
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(screenBackground()).padding(padding).padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            if (!emailSent) {
                Text("Forgot your password?", style = MaterialTheme.typography.headlineMedium, color = contentPrimary(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text("Enter the email address associated with your CoinCalm account and we'll send you a reset link.", style = MaterialTheme.typography.bodyMedium, color = contentSecondary(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(36.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it.replace(Regex("\\s"), ""); emailError = null },
                    label = { Text("Email Address") }, isError = inlineEmailError != null,
                    supportingText = inlineEmailError?.let { { Text(it, color = ErrorRed) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (isSendEnabled) {
                            scope.launch { sendResetEmail(email, { isLoading = it }, { emailError = it }, { sentToEmail = email; emailSent = true }, { msg -> snackbarHost.showSnackbar(msg) }) }
                        }
                    }),
                    singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        scope.launch { sendResetEmail(email, { isLoading = it }, { emailError = it }, { sentToEmail = email; emailSent = true }, { msg -> snackbarHost.showSnackbar(msg) }) }
                    },
                    enabled = isSendEnabled, modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    else Text("Send Reset Link", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = { navController.popBackStack() }) { Text("Back to Login", color = LimeGreen) }

            } else {
                Spacer(Modifier.height(24.dp))
                Text("Check your inbox", style = MaterialTheme.typography.headlineMedium, color = contentPrimary(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = NavyMedium), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reset link sent to:", style = MaterialTheme.typography.bodyMedium, color = contentSecondary())
                        Spacer(Modifier.height(6.dp))
                        Text(sentToEmail, style = MaterialTheme.typography.titleMedium, color = LimeGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                StepInstruction("1", "Open the email from Firebase / CoinCalm in your inbox")
                StepInstruction("2", "Tap the reset link inside the email")
                StepInstruction("3", "Set your new password on the page that opens")
                StepInstruction("4", "Come back here and sign in with your new password")
                Spacer(Modifier.height(8.dp))
                Text("Can't find it? Check your spam/junk folder.", style = MaterialTheme.typography.labelSmall, color = contentSecondary(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.ForgotPassword.route) { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest), shape = MaterialTheme.shapes.medium
                ) { Text("Back to Login", style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { emailSent = false; email = sentToEmail }) { Text("Didn't receive it? Try again", color = contentSecondary()) }
            }
        }
    }
}

@Composable
private fun StepInstruction(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(28.dp).background(LimeGreen.copy(alpha = 0.15f), MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
            Text(number, color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = contentSecondary(), modifier = Modifier.weight(1f))
    }
}

private suspend fun sendResetEmail(email: String, setLoading: (Boolean) -> Unit, setEmailError: (String?) -> Unit, onSent: () -> Unit, showSnackbar: suspend (String) -> Unit) {
    val cleanEmail = email.trim().replace(Regex("\\s"), "").lowercase()
    setLoading(true)
    try {
        FirebaseAuth.getInstance().sendPasswordResetEmail(cleanEmail).await()
        setLoading(false); onSent()
    } catch (e: FirebaseAuthInvalidUserException) {
        setLoading(false); onSent() // Security: Prevent enumeration
    } catch (e: Exception) {
        setLoading(false); showSnackbar("Failed to send reset email. Check your connection.")
    }
}