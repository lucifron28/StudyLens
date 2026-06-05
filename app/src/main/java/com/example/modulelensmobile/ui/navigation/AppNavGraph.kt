package com.example.modulelensmobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.modulelensmobile.feature.auth.LoginScreen
import com.example.modulelensmobile.feature.auth.RegisterScreen
import com.example.modulelensmobile.feature.home.HomeScreen
import com.example.modulelensmobile.feature.modules.ModuleReaderScreen
import com.example.modulelensmobile.feature.profile.ProfileScreen
import com.example.modulelensmobile.feature.scans.BoardNotesScreen
import com.example.modulelensmobile.feature.scans.OcrResultScreen
import com.example.modulelensmobile.feature.studytools.AiSummaryScreen
import com.example.modulelensmobile.feature.subjects.SubjectDetailScreen
import com.example.modulelensmobile.feature.subjects.SubjectsScreen
import com.example.modulelensmobile.ui.components.BottomNavigationBar

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        AppRoutes.HOME,
        AppRoutes.SUBJECTS,
        AppRoutes.SCANS,
        AppRoutes.PROFILE
    )

    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(AppRoutes.HOME) },
                    onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) }
                )
            }
            composable(AppRoutes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(AppRoutes.HOME) },
                    onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) }
                )
            }
            composable(AppRoutes.HOME) {
                HomeScreen()
            }
            composable(AppRoutes.SUBJECTS) {
                SubjectsScreen(
                    onNavigateToSubjectDetail = { subjectId ->
                        navController.navigate(AppRoutes.createSubjectDetailRoute(subjectId))
                    }
                )
            }
            composable(AppRoutes.SUBJECT_DETAIL) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.MODULE_READER) { backStackEntry ->
                val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                ModuleReaderScreen(
                    moduleId = moduleId,
                    onNavigateToSummary = { navController.navigate(AppRoutes.AI_SUMMARY) }
                )
            }
            composable(AppRoutes.SCANS) {
                BoardNotesScreen()
            }
            composable(AppRoutes.OCR_RESULT) {
                OcrResultScreen(
                    onSaveNote = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.AI_SUMMARY) {
                AiSummaryScreen()
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    onLogout = { navController.navigate(AppRoutes.LOGIN) }
                )
            }
        }
    }
}
