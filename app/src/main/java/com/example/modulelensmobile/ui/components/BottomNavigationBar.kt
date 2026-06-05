package com.example.modulelensmobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.modulelensmobile.ui.navigation.BottomNavItem
import com.example.modulelensmobile.ui.theme.ModuleNavy
import com.example.modulelensmobile.ui.theme.ModuleTeal

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Subjects,
        BottomNavItem.Scans,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ModuleTeal,
                    unselectedIconColor = ModuleNavy.copy(alpha = 0.6f),
                    selectedTextColor = ModuleTeal,
                    unselectedTextColor = ModuleNavy.copy(alpha = 0.6f),
                    indicatorColor = ModuleTeal.copy(alpha = 0.1f)
                )
            )
        }
    }
}
