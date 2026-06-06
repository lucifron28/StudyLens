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
import com.example.modulelensmobile.feature.modules.ModuleReaderViewModel
import com.example.modulelensmobile.feature.modules.ModuleReaderViewModelFactory
import com.example.modulelensmobile.feature.profile.ProfileScreen
import com.example.modulelensmobile.feature.scans.BoardNotesScreen
import com.example.modulelensmobile.feature.scans.BoardNotesViewModel
import com.example.modulelensmobile.feature.scans.BoardNotesViewModelFactory
import com.example.modulelensmobile.feature.scans.OcrResultScreen
import com.example.modulelensmobile.feature.scans.OcrResultViewModel
import com.example.modulelensmobile.feature.scans.OcrResultViewModelFactory
import com.example.modulelensmobile.feature.studytools.AiSummaryScreen
import com.example.modulelensmobile.feature.studytools.AiSummaryViewModel
import com.example.modulelensmobile.feature.studytools.AiSummaryViewModelFactory
import com.example.modulelensmobile.feature.subjects.SubjectDetailScreen
import com.example.modulelensmobile.feature.subjects.SubjectDetailViewModel
import com.example.modulelensmobile.feature.subjects.SubjectDetailViewModelFactory
import com.example.modulelensmobile.feature.subjects.SubjectsScreen
import com.example.modulelensmobile.feature.subjects.SubjectsViewModel
import com.example.modulelensmobile.feature.subjects.SubjectsViewModelFactory
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
                val subjectsViewModel: SubjectsViewModel = viewModel(
                    factory = SubjectsViewModelFactory(app.container.subjectsRepository)
                )
                SubjectsScreen(
                    viewModel = subjectsViewModel,
                    onNavigateToSubjectDetail = { subjectId ->
                        navController.navigate(AppRoutes.createSubjectDetailRoute(subjectId))
                    }
                )
            }
            composable(AppRoutes.SUBJECT_DETAIL) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                val subjectDetailViewModel: SubjectDetailViewModel = viewModel(
                    factory = SubjectDetailViewModelFactory(
                        subjectId = subjectId,
                        subjectsRepository = app.container.subjectsRepository
                    )
                )
                SubjectDetailScreen(
                    viewModel = subjectDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToModuleReader = { moduleId ->
                        navController.navigate(AppRoutes.createModuleReaderRoute(moduleId))
                    }
                )
            }
            composable(AppRoutes.MODULE_READER) { backStackEntry ->
                val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                val moduleReaderViewModel: ModuleReaderViewModel = viewModel(
                    factory = ModuleReaderViewModelFactory(
                        moduleId = moduleId,
                        modulesRepository = app.container.modulesRepository
                    )
                )
                ModuleReaderScreen(
                    viewModel = moduleReaderViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToSummary = {
                        navController.navigate(AppRoutes.createAiSummaryRoute("module", moduleId))
                    }
                )
            }
            composable(AppRoutes.SCANS) {
                val boardNotesViewModel: BoardNotesViewModel = viewModel(
                    factory = BoardNotesViewModelFactory(app.container.boardScansRepository)
                )
                BoardNotesScreen(
                    viewModel = boardNotesViewModel,
                    onNavigateToOcrResult = { scanId ->
                        navController.navigate(AppRoutes.createOcrResultRoute(scanId))
                    }
                )
            }
            composable(AppRoutes.OCR_RESULT) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
                val ocrResultViewModel: OcrResultViewModel = viewModel(
                    factory = OcrResultViewModelFactory(
                        scanId = scanId,
                        boardScansRepository = app.container.boardScansRepository
                    )
                )
                OcrResultScreen(
                    viewModel = ocrResultViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.AI_SUMMARY) { backStackEntry ->
                val sourceType = backStackEntry.arguments?.getString("sourceType") ?: ""
                val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                val aiSummaryViewModel: AiSummaryViewModel = viewModel(
                    factory = AiSummaryViewModelFactory(
                        sourceType = sourceType,
                        sourceId = sourceId,
                        aiRepository = app.container.aiRepository
                    )
                )
                AiSummaryScreen(
                    viewModel = aiSummaryViewModel,
                    onBack = { navController.popBackStack() }
                )
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
