package com.st10448336.coincalm.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.st10448336.coincalm.ui.theme.LimeGreen
// 1. Swap the import from TextPrimary to contentPrimary
import com.st10448336.coincalm.ui.theme.contentPrimary
import com.st10448336.coincalm.ui.theme.screenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinCalmTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        // Calling dynamic function here instead of the static color
        title = { Text(title, color = contentPrimary()) },
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
            containerColor = screenBackground()
        )
    )
}