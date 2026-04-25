package com.st10448336.coincalm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

// CoinCalm always uses its dark navy palette regardless of system setting,
// so we provide a single dark scheme and ignore the system preference.
private val CoinCalmColorScheme = darkColorScheme(
    primary          = LimeGreen,
    onPrimary        = NavyDarkest,
    primaryContainer = NavyMedium,
    background       = NavyDark,
    surface          = NavyMedium,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    secondary        = LimeGreenLight,
    onSecondary      = NavyDarkest,
    error            = ErrorRed,
    onError          = Color.White
)

/**
 * CoinCalm app-wide Compose theme.
 * Wrap the entire NavHost in this composable in MainActivity.
 */
@Composable
fun CoinCalmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoinCalmColorScheme,
        typography  = CoinCalmTypography,
        content     = content
    )
}
@Composable
fun coinCalmTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LimeGreen,
        unfocusedBorderColor = NavyLight,
        focusedLabelColor = LimeGreen,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = LimeGreen,
        errorBorderColor = ErrorRed,
        errorLabelColor = ErrorRed,
        errorCursorColor = ErrorRed,
        errorTextColor = TextPrimary,
        focusedContainerColor = NavyDarkest,
        unfocusedContainerColor = NavyDarkest,
        disabledContainerColor = NavyDarkest,
        errorContainerColor = NavyDarkest
    )
}




