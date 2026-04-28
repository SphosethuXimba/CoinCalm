package com.st10448336.coincalm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalDarkMode = compositionLocalOf { true }

@Composable
fun screenBackground(): Color =
    if (LocalDarkMode.current) NavyDark else Color(0xFFF0F4FF)

@Composable
fun contentPrimary(): Color =
    if (LocalDarkMode.current) TextPrimary else Color(0xFF0A1628)

@Composable
fun contentSecondary(): Color =
    if (LocalDarkMode.current) TextSecondary else Color(0xFF455A64)