package com.example.modulelensmobile.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppRoutes.HOME) {
        composable(AppRoutes.LOGIN) { Text("Login Screen") }
        composable(AppRoutes.REGISTER) { Text("Register Screen") }
        composable(AppRoutes.HOME) { Text("Home Screen") }
        composable(AppRoutes.SUBJECTS) { Text("Subjects Screen") }
        composable(AppRoutes.SUBJECT_DETAIL) { Text("Subject Detail Screen") }
        composable(AppRoutes.MODULE_READER) { Text("Module Reader Screen") }
        composable(AppRoutes.SCANS) { Text("Board Notes Screen") }
        composable(AppRoutes.OCR_RESULT) { Text("OCR Result Screen") }
        composable(AppRoutes.AI_SUMMARY) { Text("AI Summary Screen") }
        composable(AppRoutes.PROFILE) { Text("Profile Screen") }
    }
}
