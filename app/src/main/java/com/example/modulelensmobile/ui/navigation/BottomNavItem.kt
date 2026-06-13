package com.example.modulelensmobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(AppRoutes.HOME, "Home", Icons.Default.Home)
    object Subjects : BottomNavItem(AppRoutes.SUBJECTS, "Subjects", Icons.AutoMirrored.Filled.MenuBook)
    object Scans : BottomNavItem(AppRoutes.SCANS, "Scans", Icons.Default.PhotoCamera)
    object Profile : BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Default.Person)
}
