package com.st10448336.coincalm.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.R
import com.st10448336.coincalm.data.SessionPreferences
import com.st10448336.coincalm.data.UserSyncHelper
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.delay

/**
 * SplashScreen — Entry point with smart session routing.
 *
 * ROUTING LOGIC:
 *
 *  Firebase session exists + Remember Me = TRUE
 *    → Sync RoomDB → go to Dashboard  (user chose to stay logged in)
 *
 *  Firebase session exists + Remember Me = FALSE
 *    → Sign out Firebase → go to Login (cloned device / shared phone)
 *
 *  No Firebase session
 *    → go to Login
 *
 * This prevents the "skipped straight to Dashboard" bug when a team member
 * clones the repo and runs it on their own device.
 */
@Composable
fun SplashScreen(navController: NavController) {

    val context     = LocalContext.current
    val scale       = remember { Animatable(0f) }
    val sessionPrefs = remember { SessionPreferences(context) }

    LaunchedEffect(key1 = true) {
        // Animate the logo
        scale.animateTo(
            targetValue   = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )

        delay(1500L)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val rememberMe   = sessionPrefs.isRememberMe()

        when {
            // Case 1: Logged in AND user chose to stay signed in
            firebaseUser != null && rememberMe -> {
                // Ensure RoomDB has this user (handles new device / clone)
                UserSyncHelper.ensureUserInRoomDb(context)
                navController.navigate(NavRoutes.Dashboard.route) {
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
            }

            // Case 2: Firebase has a session but Remember Me is OFF
            // This happens when:
            //  - A team member clones the repo (Firebase has no token → goes to Login anyway)
            //  - The user logged in on this device but didn't tick Remember Me
            //  - The APK is installed on a shared / new phone
            firebaseUser != null && !rememberMe -> {
                // Discard the persisted Firebase token — force login
                FirebaseAuth.getInstance().signOut()
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
            }

            //  Case 3: No Firebase session at all
            else -> {
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
            }
        }
    }

    //  UI
    Column(
        modifier              = Modifier
            .fillMaxSize()
            .background(NavyDark),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Image(
            painter            = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "CoinCalm Logo",
            modifier           = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "CoinCalm",
            style      = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
            modifier   = Modifier.scale(scale.value)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "Control your coins, calm your mind.",
            style    = MaterialTheme.typography.titleMedium,
            color    = LimeGreen,
            modifier = Modifier.scale(scale.value)
        )
    }
}