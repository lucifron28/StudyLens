package com.example.studylensmobile.ui.navigation

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
import com.example.studylensmobile.StudyLensApp
import com.example.studylensmobile.core.viewmodel.viewModelFactory
import com.example.studylensmobile.feature.auth.AuthViewModel
import com.example.studylensmobile.feature.auth.LoginScreen
import com.example.studylensmobile.feature.auth.RegisterScreen
import com.example.studylensmobile.feature.home.HomeScreen
import com.example.studylensmobile.feature.home.HomeViewModel
import com.example.studylensmobile.feature.modules.ModuleReaderScreen
import com.example.studylensmobile.feature.modules.ModuleReaderViewModel
import com.example.studylensmobile.feature.profile.ProfileScreen
import com.example.studylensmobile.feature.scans.BoardNotesScreen
import com.example.studylensmobile.feature.scans.BoardNotesViewModel
import com.example.studylensmobile.feature.scans.OcrResultScreen
import com.example.studylensmobile.feature.scans.OcrResultViewModel
import com.example.studylensmobile.feature.studytools.AiSummaryScreen
import com.example.studylensmobile.feature.studytools.AiSummaryViewModel
import com.example.studylensmobile.feature.studytools.FlashcardsScreen
import com.example.studylensmobile.feature.studytools.FlashcardsViewModel
import com.example.studylensmobile.feature.studytools.QuizScreen
import com.example.studylensmobile.feature.studytools.QuizViewModel
import com.example.studylensmobile.feature.subjects.SubjectDetailScreen
import com.example.studylensmobile.feature.subjects.SubjectDetailViewModel
import com.example.studylensmobile.feature.subjects.SubjectsScreen
import com.example.studylensmobile.feature.subjects.SubjectsViewModel
import com.example.studylensmobile.ui.components.BottomNavigationBar

@Composable
fun AppNavGraph(navController: NavHostController, app: StudyLensApp) {
    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory { AuthViewModel(app.container.authRepository) }
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
                    factory = viewModelFactory {
                        HomeViewModel(
                            dashboardRepository = app.container.dashboardRepository,
                            authRepository = app.container.authRepository
                        )
                    }
                )
                HomeScreen(viewModel = homeViewModel)
            }
            composable(AppRoutes.SUBJECTS) {
                val subjectsViewModel: SubjectsViewModel = viewModel(
                    factory = viewModelFactory {
                        SubjectsViewModel(app.container.subjectsRepository)
                    }
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
                    factory = viewModelFactory {
                        SubjectDetailViewModel(
                            subjectId = subjectId,
                            subjectsRepository = app.container.subjectsRepository
                        )
                    }
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
                    factory = viewModelFactory {
                        ModuleReaderViewModel(
                            moduleId = moduleId,
                            modulesRepository = app.container.modulesRepository
                        )
                    }
                )
                ModuleReaderScreen(
                    viewModel = moduleReaderViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToSummary = {
                        navController.navigate(AppRoutes.createAiSummaryRoute("module", moduleId))
                    },
                    onNavigateToFlashcards = {
                        navController.navigate(AppRoutes.createFlashcardsRoute("module", moduleId))
                    }
                )
            }
            composable(AppRoutes.SCANS) {
                val boardNotesViewModel: BoardNotesViewModel = viewModel(
                    factory = viewModelFactory {
                        BoardNotesViewModel(app.container.boardScansRepository)
                    }
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
                    factory = viewModelFactory {
                        OcrResultViewModel(
                            scanId = scanId,
                            boardScansRepository = app.container.boardScansRepository
                        )
                    }
                )
                OcrResultScreen(
                    viewModel = ocrResultViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToSummary = { boardScanId ->
                        navController.navigate(AppRoutes.createAiSummaryRoute("board_scan", boardScanId))
                    }
                )
            }
            composable(AppRoutes.AI_SUMMARY) { backStackEntry ->
                val sourceType = backStackEntry.arguments?.getString("sourceType") ?: ""
                val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                val aiSummaryViewModel: AiSummaryViewModel = viewModel(
                    factory = viewModelFactory {
                        AiSummaryViewModel(
                            sourceType = sourceType,
                            sourceId = sourceId,
                            aiRepository = app.container.aiRepository
                        )
                    }
                )
                AiSummaryScreen(
                    viewModel = aiSummaryViewModel,
                    onBack = { navController.popBackStack() },
                    onCreateFlashcards = { type, id ->
                        navController.navigate(AppRoutes.createFlashcardsRoute(type, id))
                    },
                    onPracticeQuiz = { type, id ->
                        navController.navigate(AppRoutes.createQuizRoute(type, id))
                    }
                )
            }
            composable(AppRoutes.FLASHCARDS) { backStackEntry ->
                val sourceType = backStackEntry.arguments?.getString("sourceType") ?: ""
                val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                val flashcardsViewModel: FlashcardsViewModel = viewModel(
                    factory = viewModelFactory {
                        FlashcardsViewModel(
                            sourceType = sourceType,
                            sourceId = sourceId,
                            aiRepository = app.container.aiRepository
                        )
                    }
                )
                FlashcardsScreen(
                    viewModel = flashcardsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.QUIZ) { backStackEntry ->
                val sourceType = backStackEntry.arguments?.getString("sourceType") ?: ""
                val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                val quizViewModel: QuizViewModel = viewModel(
                    factory = viewModelFactory {
                        QuizViewModel(
                            sourceType = sourceType,
                            sourceId = sourceId,
                            aiRepository = app.container.aiRepository
                        )
                    }
                )
                QuizScreen(
                    viewModel = quizViewModel,
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
