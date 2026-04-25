package com.st10448336.coincalm.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.R
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDark
import com.st10448336.coincalm.ui.theme.TextPrimary
import com.st10448336.coincalm.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // 1. Set up a scale animation for a cool "pop-in" effect
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Animate the logo and text scaling up
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )

        // 2. Pause for 2 seconds so the user sees your logo
        delay(2000L)

        // 3. SMART ROUTING: Check Firebase to see if they are already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navController.navigate(NavRoutes.Dashboard.route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
            }
        }
    }

    // 4. The actual UI layout (Changed to a Column to stack items)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "CoinCalm Logo",
            modifier = Modifier
                .size(200.dp) // Slightly smaller to make room for text
                .scale(scale.value)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Name
        Text(
            text = "CoinCalm",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.scale(scale.value)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Slogan
        Text(
            text = "Control your coins, calm your mind.",
            style = MaterialTheme.typography.titleMedium,
            color = LimeGreen, // Or swap to TextSecondary if you prefer it subtler!
            modifier = Modifier.scale(scale.value)
        )
    }
}