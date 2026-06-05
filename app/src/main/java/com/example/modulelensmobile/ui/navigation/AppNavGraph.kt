package com.example.modulelensmobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.modulelensmobile.ModuleLensApp
import com.example.modulelensmobile.feature.auth.AuthViewModel
import com.example.modulelensmobile.feature.auth.AuthViewModelFactory
import com.example.modulelensmobile.feature.auth.LoginScreen
import com.example.modulelensmobile.feature.auth.RegisterScreen
import com.example.modulelensmobile.feature.home.HomeScreen
import com.example.modulelensmobile.feature.home.HomeViewModel
import com.example.modulelensmobile.feature.home.HomeViewModelFactory
import com.example.modulelensmobile.feature.modules.ModuleReaderScreen
import com.example.modulelensmobile.feature.profile.ProfileScreen
import com.example.modulelensmobile.feature.scans.BoardNotesScreen
import com.example.modulelensmobile.feature.scans.OcrResultScreen
import com.example.modulelensmobile.feature.studytools.AiSummaryScreen
import com.example.modulelensmobile.feature.subjects.SubjectDetailScreen
import com.example.modulelensmobile.feature.subjects.SubjectsScreen
import com.example.modulelensmobile.ui.components.BottomNavigationBar

@Composable
fun AppNavGraph(navController: NavHostController, app: ModuleLensApp) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(app.container.authRepository)
    )
    val accessToken by app.container.tokenManager.accessToken
        .collectAsState(initial = "")

    val startDestination = if (accessToken.isNullOrEmpty()) AppRoutes.LOGIN else AppRoutes.HOME

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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) }
                )
            }
            composable(AppRoutes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        dashboardRepository = app.container.dashboardRepository,
                        authRepository = app.container.authRepository
                    )
                )
                HomeScreen(viewModel = homeViewModel)
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
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
