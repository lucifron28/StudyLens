package com.example.studylensmobile.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.example.studylensmobile.feature.modules.PdfReaderScreen
import com.example.studylensmobile.feature.profile.EditProfileScreen
import com.example.studylensmobile.feature.profile.EditProfileViewModel
import com.example.studylensmobile.feature.profile.ProfileScreen
import com.example.studylensmobile.feature.profile.ProfileViewModel
import com.example.studylensmobile.feature.scans.BoardNotesScreen
import com.example.studylensmobile.feature.scans.BoardNotesViewModel
import com.example.studylensmobile.feature.scans.CameraCaptureScreen
import com.example.studylensmobile.feature.scans.ImageCropScreen
import com.example.studylensmobile.feature.scans.OcrResultScreen
import com.example.studylensmobile.feature.scans.OcrResultViewModel
import com.example.studylensmobile.feature.studytools.AiSummaryScreen
import com.example.studylensmobile.feature.studytools.AiSummaryViewModel
import com.example.studylensmobile.feature.studytools.FlashcardsScreen
import com.example.studylensmobile.feature.studytools.FlashcardsViewModel
import com.example.studylensmobile.feature.studytools.QuizScreen
import com.example.studylensmobile.feature.studytools.QuizViewModel
import com.example.studylensmobile.feature.studytools.TutorScreen
import com.example.studylensmobile.feature.studytools.TutorViewModel
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
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSubjectDetail = { subjectId ->
                        navController.navigate(AppRoutes.createSubjectDetailRoute(subjectId.toString()))
                    }
                )
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
                    },
                    onNavigateToQuiz = {
                        navController.navigate(AppRoutes.createQuizRoute("module", moduleId))
                    },
                    onNavigateToTutor = {
                        navController.navigate(AppRoutes.createTutorRoute("module", moduleId))
                    },
                    onNavigateToPdfViewer = { url ->
                        val moduleTitle = moduleReaderViewModel.uiState.value.module?.title ?: "Document"
                        navController.navigate(AppRoutes.createPdfViewerRoute(moduleTitle, url))
                    }
                )
            }
            composable(AppRoutes.PDF_VIEWER) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "Document"
                val url = backStackEntry.arguments?.getString("url") ?: ""
                PdfReaderScreen(
                    title = title,
                    url = url,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.SCANS) { backStackEntry ->
                val boardNotesViewModel: BoardNotesViewModel = viewModel(
                    factory = viewModelFactory {
                        BoardNotesViewModel(
                            boardScansRepository = app.container.boardScansRepository,
                            subjectsRepository = app.container.subjectsRepository
                        )
                    }
                )
                
                // Observe camera capture results
                val capturedImageUri = backStackEntry.savedStateHandle.get<String>("captured_image_uri")
                val context = LocalContext.current
                LaunchedEffect(capturedImageUri) {
                    if (capturedImageUri != null) {
                        boardNotesViewModel.recognizeBoardImage(context, Uri.parse(capturedImageUri))
                        // Clear it so we don't trigger it again
                        backStackEntry.savedStateHandle.remove<String>("captured_image_uri")
                    }
                }

                BoardNotesScreen(
                    viewModel = boardNotesViewModel,
                    onNavigateToOcrResult = { scanId ->
                        navController.navigate(AppRoutes.createOcrResultRoute(scanId))
                    },
                    onNavigateToCamera = {
                        navController.navigate(AppRoutes.CAMERA_CAPTURE)
                    },
                    onNavigateToSummary = { boardScanId ->
                        navController.navigate(AppRoutes.createAiSummaryRoute("board_scan", boardScanId))
                    },
                    onNavigateToFlashcards = { boardScanId ->
                        navController.navigate(AppRoutes.createFlashcardsRoute("board_scan", boardScanId))
                    },
                    onNavigateToQuiz = { boardScanId ->
                        navController.navigate(AppRoutes.createQuizRoute("board_scan", boardScanId))
                    },
                    onNavigateToTutor = { boardScanId ->
                        navController.navigate(AppRoutes.createTutorRoute("board_scan", boardScanId))
                    }
                )
            }
            composable(AppRoutes.CAMERA_CAPTURE) {
                val context = LocalContext.current

                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasCameraPermission = granted }

                CameraCaptureScreen(
                    hasPermission = hasCameraPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onImageCaptured = { uri ->
                        navController.navigate(AppRoutes.createImageCropRoute(uri.toString()))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.IMAGE_CROP) { backStackEntry ->
                val imageUri = Uri.decode(backStackEntry.arguments?.getString("imageUri").orEmpty())

                ImageCropScreen(
                    imageUri = imageUri,
                    onBack = { navController.popBackStack() },
                    onCropConfirmed = { croppedUri ->
                        navController
                            .getBackStackEntry(AppRoutes.SCANS)
                            .savedStateHandle
                            .set("captured_image_uri", croppedUri.toString())
                        navController.popBackStack(AppRoutes.SCANS, inclusive = false)
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
                    },
                    onNavigateToFlashcards = { boardScanId ->
                        navController.navigate(AppRoutes.createFlashcardsRoute("board_scan", boardScanId))
                    },
                    onNavigateToQuiz = { boardScanId ->
                        navController.navigate(AppRoutes.createQuizRoute("board_scan", boardScanId))
                    },
                    onNavigateToTutor = { boardScanId ->
                        navController.navigate(AppRoutes.createTutorRoute("board_scan", boardScanId))
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
                    },
                    onAskTutor = { type, id ->
                        navController.navigate(AppRoutes.createTutorRoute(type, id))
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
            composable(AppRoutes.TUTOR) { backStackEntry ->
                val sourceType = backStackEntry.arguments?.getString("sourceType") ?: ""
                val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
                val tutorViewModel: TutorViewModel = viewModel(
                    factory = viewModelFactory {
                        TutorViewModel(
                            sourceType = sourceType,
                            sourceId = sourceId,
                            aiRepository = app.container.aiRepository
                        )
                    }
                )
                TutorScreen(
                    viewModel = tutorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = viewModelFactory { ProfileViewModel(app.container.authRepository, app.container.themePreferences) }
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToEditProfile = {
                        navController.navigate(AppRoutes.EDIT_PROFILE)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoutes.EDIT_PROFILE) {
                val editProfileViewModel: EditProfileViewModel = viewModel(
                    factory = viewModelFactory { EditProfileViewModel(app.container.authRepository) }
                )
                EditProfileScreen(
                    viewModel = editProfileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
