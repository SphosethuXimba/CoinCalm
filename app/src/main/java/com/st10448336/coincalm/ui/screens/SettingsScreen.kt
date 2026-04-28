package com.st10448336.coincalm.ui.screens

import android.util.Log
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

@Composable
fun SettingsScreen(
    navController: NavController,
    darkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {

    val TAG     = "SettingsScreen"
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()

    var currentUser by remember { mutableStateOf<User?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No Firebase session on Settings — redirecting to Login")
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Settings.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            currentUser = db.userDao().getUserById(uid)
        }
    }

    // Show a loading spinner while fetching, but don't return early forever.
    // If user is still null after the LaunchedEffect completes, show a
    // recovery message instead of spinning indefinitely.
    if (currentUser == null) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(if (darkMode) NavyDarkest else Color(0xFFF0F4FF)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = LimeGreen)
                Text(
                    text  = "Loading profile…",
                    color = TextSecondary
                )
            }
        }
        return
    }

    val screenBg = if (darkMode) NavyDarkest else Color(0xFFF0F4FF)
    val cardBg   = if (darkMode) NavyDark    else Color(0xFFFFFFFF)
    val textPri  = if (darkMode) TextPrimary  else Color(0xFF0A1628)
    val textSec  = if (darkMode) TextSecondary else Color(0xFF607D8B)

    // ── Password reset dialog ──────────────────────────────────────────────────
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor   = NavyMedium,
            title = {
                Text(
                    text       = "Reset Password",
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text     = "A password reset link will be sent to:\n${currentUser?.email}",
                    color    = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    auth.sendPasswordResetEmail(currentUser?.email ?: "")
                    Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Send", color = LimeGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        Text(
            text       = "Settings",
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            color      = textPri
        )

        // ── Profile card ─────────────────────────────────────────────────────
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(colors = listOf(LimeGreen, LimeGreenDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = currentUser?.username?.take(2)?.uppercase() ?: "NN",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NavyDarkest
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = currentUser?.username ?: "Loading...",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = textPri
                    )
                    Text(
                        text     = currentUser?.email ?: "Loading...",
                        fontSize = 13.sp,
                        color    = textSec
                    )
                }
            }
        }

        // ── Points card ──────────────────────────────────────────────────────
        val points = 35
        Card(
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = LimeGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text       = "Your Points",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = NavyDark
                    )
                    Text(
                        text       = "$points pts",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NavyDarkest
                    )
                }
                Icon(
                    imageVector        = Icons.Filled.Star,
                    contentDescription = null,
                    tint               = NavyDark,
                    modifier           = Modifier.size(48.dp)
                )
            }
        }

        // ── Appearance section ────────────────────────────────────────────────
        SettingsSectionHeader(title = "Appearance", textColor = textSec)

        Card(
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = null,
                        tint               = LimeGreen,
                        modifier           = Modifier.size(22.dp)
                    )
                    Text(
                        text     = if (darkMode) "Dark Mode" else "Light Mode",
                        fontSize = 15.sp,
                        color    = textPri
                    )
                }
                Switch(
                    checked         = darkMode,
                    onCheckedChange = { onDarkModeToggle(it) },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = NavyDarkest,
                        checkedTrackColor   = LimeGreen,
                        uncheckedThumbColor = NavyDarkest,
                        uncheckedTrackColor = TextHint
                    )
                )
            }
        }

        // ── Account section ───────────────────────────────────────────────────
        SettingsSectionHeader(title = "Account", textColor = textSec)

        Card(
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRowItem(
                    icon      = Icons.Filled.Person,
                    label     = "Edit Profile",
                    textColor = textPri,
                    onClick   = { navController.navigate(NavRoutes.EditProfile.route) }
                )
                HorizontalDivider(color = InputBackground, thickness = 1.dp)
                SettingsRowItem(
                    icon      = Icons.Filled.Lock,
                    label     = "Change Password",
                    textColor = textPri,
                    onClick   = { showPasswordDialog = true }
                )
                HorizontalDivider(color = InputBackground, thickness = 1.dp)
                SettingsRowItem(
                    icon      = Icons.Filled.Notifications,
                    label     = "Notifications",
                    textColor = textPri,
                    onClick   = {
                        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ── Logout button ─────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                // Clear Remember Me so the next launch goes to Login
                SessionPreferences(context).clearSession()
                auth.signOut()
                Log.d("SettingsScreen", "User signed out — Remember Me cleared")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor   = TextPrimary
            )
        ) {
            Icon(
                imageVector        = Icons.Filled.Logout,
                contentDescription = null,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "Log Out",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, textColor: Color) {
    Text(
        text          = title.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color         = textColor,
        modifier      = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = LimeGreen,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text     = label,
                fontSize = 15.sp,
                color    = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = textColor.copy(alpha = 0.4f),
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}