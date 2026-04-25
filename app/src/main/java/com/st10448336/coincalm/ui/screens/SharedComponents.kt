package com.st10448336.coincalm.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.st10448336.coincalm.ui.theme.LimeGreen
import com.st10448336.coincalm.ui.theme.NavyDarkest
import com.st10448336.coincalm.ui.theme.TextPrimary

/**
 * CoinCalmTopBar — reusable top app bar used by all child screens.
 * Displays the screen title and a back arrow that calls [onBack].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinCalmTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = TextPrimary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint               = LimeGreen
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyDarkest
        )
    )
}