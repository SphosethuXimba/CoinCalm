package com.st10448336.coincalm.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.st10448336.coincalm.ui.theme.contentPrimary
import com.st10448336.coincalm.ui.theme.screenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinCalmTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = contentPrimary()) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = contentPrimary()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBackground())
    )
}