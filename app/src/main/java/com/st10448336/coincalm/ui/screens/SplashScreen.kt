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

/** Verifies Firebase Auth session and routes to Dashboard or Login. */
@Composable
fun SplashScreen(navController: NavController) {
    val context      = LocalContext.current
    val scale        = remember { Animatable(0f) }
    val sessionPrefs = remember { SessionPreferences(context) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue   = 1f,
            animationSpec = tween(1000, easing = { OvershootInterpolator(2f).getInterpolation(it) })
        )
        delay(1500L)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val rememberMe   = sessionPrefs.isRememberMe()

        when {
            firebaseUser != null && rememberMe -> {
                UserSyncHelper.ensureUserInRoomDb(context)
                navController.navigate(NavRoutes.Dashboard.route) { popUpTo(NavRoutes.Splash.route) { inclusive = true } }
            }
            firebaseUser != null && !rememberMe -> {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Splash.route) { inclusive = true } }
            }
            else -> {
                navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Splash.route) { inclusive = true } }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(NavyDark),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "CoinCalm Logo",
            modifier = Modifier.size(200.dp).scale(scale.value)
        )
        Spacer(Modifier.height(16.dp))
        Text("CoinCalm", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.scale(scale.value))
        Spacer(Modifier.height(8.dp))
        Text("Control your coins, calm your mind.", style = MaterialTheme.typography.titleMedium, color = LimeGreen, modifier = Modifier.scale(scale.value))
    }
}