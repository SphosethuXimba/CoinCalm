package com.st10448336.coincalm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

private val CoinCalmDarkScheme = darkColorScheme(
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

private val CoinCalmLightScheme = lightColorScheme(
    primary          = LimeGreen,
    onPrimary        = NavyDarkest,
    primaryContainer = NavyMedium,
    background       = Color(0xFFF0F4FF),
    surface          = Color(0xFFFFFFFF),
    onBackground     = Color(0xFF0A1628),
    onSurface        = Color(0xFF0A1628),
    secondary        = LimeGreenLight,
    onSecondary      = NavyDarkest,
    error            = ErrorRed,
    onError          = Color.White
)

@Composable
fun CoinCalmTheme(
    darkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkMode) CoinCalmDarkScheme else CoinCalmLightScheme,
        typography  = CoinCalmTypography,
        content     = content
    )
}

@Composable
fun coinCalmTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = LimeGreen,
        unfocusedBorderColor    = NavyLight,
        focusedLabelColor       = LimeGreen,
        unfocusedLabelColor     = TextSecondary,
        focusedTextColor        = TextPrimary,
        unfocusedTextColor      = TextPrimary,
        cursorColor             = LimeGreen,
        errorBorderColor        = ErrorRed,
        errorLabelColor         = ErrorRed,
        errorCursorColor        = ErrorRed,
        errorTextColor          = TextPrimary,
        focusedContainerColor   = NavyDarkest,
        unfocusedContainerColor = NavyDarkest,
        disabledContainerColor  = NavyDarkest,
        errorContainerColor     = NavyDarkest
    )
}