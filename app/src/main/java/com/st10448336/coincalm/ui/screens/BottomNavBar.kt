package com.st10448336.coincalm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.st10448336.coincalm.navigation.NavRoutes
import com.st10448336.coincalm.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar(containerColor = NavyDark, contentColor = TextPrimary) {
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(NavRoutes.Dashboard.route) {
                    popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LimeGreen, selectedTextColor = LimeGreen,
                unselectedIconColor = TextPrimary, unselectedTextColor = TextPrimary,
                indicatorColor = NavyMedium
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(NavRoutes.Camera.route) {
                    popUpTo(NavRoutes.Camera.route) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.CameraAlt, contentDescription = "Camera") },
            label = { Text("Camera") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LimeGreen, selectedTextColor = LimeGreen,
                unselectedIconColor = TextPrimary, unselectedTextColor = TextPrimary,
                indicatorColor = NavyMedium
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(NavRoutes.Settings.route) {
                    popUpTo(NavRoutes.Settings.route) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LimeGreen, selectedTextColor = LimeGreen,
                unselectedIconColor = TextPrimary, unselectedTextColor = TextPrimary,
                indicatorColor = NavyMedium
            )
        )
    }
}