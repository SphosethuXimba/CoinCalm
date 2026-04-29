package com.st10448336.coincalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.SessionPreferences
import com.st10448336.coincalm.data.entites.User
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Master settings dashboard. Manages secure in-app password changes utilizing Firebase Re-authentication. */
@Composable
fun SettingsScreen(navController: NavController, darkMode: Boolean, onDarkModeToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) { navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Settings.route) { inclusive = true } }; return@LaunchedEffect }
        withContext(Dispatchers.IO) { currentUser = AppDatabase.getInstance(context).userDao().getUserById(uid) }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize().background(if (darkMode) NavyDarkest else Color(0xFFF0F4FF)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = LimeGreen)
                Text("Loading profile…", color = TextSecondary)
            }
        }
        return
    }

    val screenBg = if (darkMode) NavyDarkest else Color(0xFFF0F4FF)
    val cardBg   = if (darkMode) NavyDark    else Color(0xFFFFFFFF)
    val textPri  = if (darkMode) TextPrimary  else Color(0xFF0A1628)
    val textSec  = if (darkMode) TextSecondary else Color(0xFF607D8B)

    if (showPasswordDialog) {
        InAppPasswordChangeDialog(
            onDismiss = { showPasswordDialog = false },
            onForgotPassword = {
                showPasswordDialog = false
                SessionPreferences(context).clearSession()
                auth.currentUser?.email?.let { email ->
                    auth.sendPasswordResetEmail(email).addOnSuccessListener {
                        Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                    }
                }
                auth.signOut()
                navController.navigate(NavRoutes.Login.route) { popUpTo(0) { inclusive = true } }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(screenBg).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textPri)

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Brush.radialGradient(colors = listOf(LimeGreen, LimeGreenDark))), contentAlignment = Alignment.Center) {
                    Text(currentUser?.username?.take(2)?.uppercase() ?: "NN", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NavyDarkest)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(currentUser?.username ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPri)
                    Text(currentUser?.email ?: "Loading...", fontSize = 13.sp, color = textSec)
                }
            }
        }

        SettingsSectionHeader("Appearance", textSec)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(22.dp))
                    Text(if (darkMode) "Dark Mode" else "Light Mode", fontSize = 15.sp, color = textPri)
                }
                Switch(checked = darkMode, onCheckedChange = { onDarkModeToggle(it) }, colors = SwitchDefaults.colors(checkedThumbColor = NavyDarkest, checkedTrackColor = LimeGreen, uncheckedThumbColor = NavyDarkest, uncheckedTrackColor = TextHint))
            }
        }

        SettingsSectionHeader("Account", textSec)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsRowItem(Icons.Filled.Person, "Edit Profile", textPri) { navController.navigate(NavRoutes.EditProfile.route) }
                HorizontalDivider(color = InputBackground, thickness = 1.dp)
                SettingsRowItem(Icons.Filled.Lock, "Change Password", textPri) { showPasswordDialog = true }
                HorizontalDivider(color = InputBackground, thickness = 1.dp)
                SettingsRowItem(Icons.Filled.Notifications, "Notifications", textPri) { Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show() }
            }
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                SessionPreferences(context).clearSession()
                auth.signOut()
                navController.navigate(NavRoutes.Login.route) { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = TextPrimary)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, textColor: Color) { Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = textColor, modifier = Modifier.padding(horizontal = 4.dp)) }

@Composable
private fun SettingsRowItem(icon: ImageVector, label: String, textColor: Color, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 15.sp, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = textColor.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

// Attribution: Firebase Re-authentication & Password Update
// Link: https://firebase.google.com/docs/auth/android/manage-users#re-authenticate_a_user
// Author: Google Developers
@Composable
fun InAppPasswordChangeDialog(onDismiss: () -> Unit, onForgotPassword: () -> Unit = {}) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible  by remember { mutableStateOf(false) }
    var newVisible      by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var currentPwError  by remember { mutableStateOf<String?>(null) }

    val inlineNewPwError = if (newPassword.isNotEmpty() && newPassword.length < 8) "Min 8 characters" else null
    val inlineConfirmError = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) "Passwords do not match" else null
    val isSaveEnabled = currentPassword.isNotBlank() && newPassword.length >= 8 && confirmPassword == newPassword && !isLoading

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavyMedium), modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Change Password", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Enter your current password to confirm your identity, then choose a new one.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                OutlinedTextField(
                    value = currentPassword, onValueChange = { currentPassword = it; currentPwError = null }, label = { Text("Current Password") }, isError = currentPwError != null, supportingText = currentPwError?.let { { Text(it, color = ErrorRed) } },
                    visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { currentVisible = !currentVisible }) { Text(if (currentVisible) "Hide" else "Show", color = LimeGreen) } }, singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, isError = inlineNewPwError != null, supportingText = inlineNewPwError?.let { { Text(it, color = ErrorRed) } },
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { newVisible = !newVisible }) { Text(if (newVisible) "Hide" else "Show", color = LimeGreen) } }, singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm New Password") }, isError = inlineConfirmError != null, supportingText = inlineConfirmError?.let { { Text(it, color = ErrorRed) } },
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { confirmVisible = !confirmVisible }) { Text(if (confirmVisible) "Hide" else "Show", color = LimeGreen) } }, singleLine = true, enabled = !isLoading, colors = coinCalmTextFieldColors(), modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = { if (!isLoading) onForgotPassword() }, modifier = Modifier.align(Alignment.End)) { Text("Forgot current password?", color = LimeGreen, style = MaterialTheme.typography.labelSmall) }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (!isLoading) onDismiss() }, enabled = !isLoading, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, TextHint), shape = RoundedCornerShape(12.dp)) { Text("Cancel", color = TextSecondary) }
                    Button(
                        onClick = {
                            scope.launch { performPasswordChange(context, currentPassword, newPassword, { isLoading = it }, { currentPwError = it }, { Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show(); onDismiss() }) }
                        },
                        enabled = isSaveEnabled, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDarkest, disabledContainerColor = NavyLight, disabledContentColor = TextHint), shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = NavyDarkest, modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private suspend fun performPasswordChange(context: android.content.Context, currentPassword: String, newPassword: String, setLoading: (Boolean) -> Unit, setCurrentPwError: (String?) -> Unit, onSuccess: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val email = currentUser.email ?: return
    setLoading(true)
    try {
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        currentUser.reauthenticate(credential).await()
        currentUser.updatePassword(newPassword).await()
        setLoading(false)
        withContext(Dispatchers.Main) { onSuccess() }
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        setLoading(false); withContext(Dispatchers.Main) { setCurrentPwError("Incorrect current password") }
    } catch (e: FirebaseAuthWeakPasswordException) {
        setLoading(false); withContext(Dispatchers.Main) { Toast.makeText(context, "New password is too weak.", Toast.LENGTH_LONG).show() }
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        setLoading(false); withContext(Dispatchers.Main) { Toast.makeText(context, "Session expired. Please log out and back in.", Toast.LENGTH_LONG).show() }
    } catch (e: Exception) {
        setLoading(false); withContext(Dispatchers.Main) { Toast.makeText(context, "Failed to update password.", Toast.LENGTH_LONG).show() }
    }
}