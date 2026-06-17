# Android File Map

This file explains the current Kotlin files in the Android app. Use it as a quick reference when presenting the codebase or when adding a new feature.

## App Entry

| File | Purpose |
| --- | --- |
| `MainActivity.kt` | Launches the app and sets the Compose content. |
| `StudyLensApp.kt` | Custom `Application` class that creates the shared `AppContainer`. |

Why these files exist:

- Android needs one entry activity.
- The application class gives the app one shared place to initialize repositories and networking.

## Core

| File | Purpose |
| --- | --- |
| `core/datastore/TokenManager.kt` | Stores and reads JWT access and refresh tokens using DataStore. |
| `core/format/TextFormatters.kt` | Contains shared text/date formatting helpers used across screens. |
| `core/network/AppContainer.kt` | Creates Retrofit APIs and repositories for the whole app. |
| `core/network/AuthInterceptor.kt` | Adds the `Authorization: Bearer ...` header to protected requests. |
| `core/network/RetrofitClient.kt` | Builds Retrofit, OkHttp, JSON conversion, logging, and auth handling. |
| `core/network/TokenRefreshAuthenticator.kt` | Refreshes expired access tokens when the backend returns an auth failure. |
| `core/viewmodel/ViewModelFactory.kt` | Provides a small reusable ViewModel factory helper. |

Why this folder exists:

- These files are shared by many features.
- Keeping them in `core` prevents every screen from creating its own networking, token, or formatting code.

## Remote APIs

| File | Purpose |
| --- | --- |
| `data/remote/api/AuthApi.kt` | Defines register, login, token refresh, and current-user API calls. |
| `data/remote/api/LearningApi.kt` | Defines dashboard, subjects, modules, chapters, and board scan API calls. |
| `data/remote/api/AiApi.kt` | Defines summary, flashcard, and quiz generation API calls. |
| `data/remote/ApiResult.kt` | Converts Retrofit responses into clean `Result<T>` values. |

Why this folder exists:

- Retrofit APIs make backend calls readable.
- Route paths stay in one place instead of being scattered through screens.

## DTOs

| File | Purpose |
| --- | --- |
| `data/remote/dto/AuthDtos.kt` | Request and response classes for auth endpoints. |
| `data/remote/dto/DashboardDtos.kt` | Response classes for the home dashboard. |
| `data/remote/dto/SubjectDtos.kt` | Response classes for subjects and subject overview. |
| `data/remote/dto/ModuleDtos.kt` | Response classes for module details and chapters. |
| `data/remote/dto/BoardScanDtos.kt` | Request and response classes for board scans and OCR results. |
| `data/remote/dto/AiDtos.kt` | Request and response classes for AI study-tool endpoints. |

Why DTOs are separate:

- DTOs follow the backend JSON shape.
- UI code can use cleaner domain models instead of raw API response objects.

## Repositories

| File | Purpose |
| --- | --- |
| `data/repository/AuthRepository.kt` | Handles auth API calls and token saving/clearing. |
| `data/repository/DashboardRepository.kt` | Loads and maps dashboard data for the home screen. |
| `data/repository/SubjectsRepository.kt` | Loads subject lists and subject detail overview data. |
| `data/repository/ModulesRepository.kt` | Loads module details and chapter lists. |
| `data/repository/BoardScansRepository.kt` | Loads board scans, OCR detail, and board scan updates. |
| `data/repository/AiRepository.kt` | Calls backend AI endpoints and maps generated study tools. |

Why repositories are used:

- They keep API and mapping details out of ViewModels.
- ViewModels can call simple functions like `getDashboard()` or `generateSummary()`.
- Error handling stays consistent through `Result<T>`.

## Domain Models

| File | Purpose |
| --- | --- |
| `domain/model/User.kt` | Logged-in student profile data. |
| `domain/model/Dashboard.kt` | Home dashboard summary, upcoming items, continue-learning items, and activity data. |
| `domain/model/Subject.kt` | Subject list and subject overview data used by subject screens. |
| `domain/model/LearningModule.kt` | Module and chapter data used by the module reader. |
| `domain/model/BoardScan.kt` | Board scan and OCR result data used by scan screens. |
| `domain/model/Summary.kt` | AI-generated summary data. |
| `domain/model/Flashcard.kt` | AI-generated flashcard data. |
| `domain/model/Quiz.kt` | AI-generated quiz and question data. |
| `domain/model/SubjectPost.kt` | Announcement/post item used in subject detail flows. |

Why domain models are used:

- They describe what the app needs to display.
- They keep the UI stable even if backend field names change later.

## Auth Feature

| File | Purpose |
| --- | --- |
| `feature/auth/LoginScreen.kt` | Compose UI for login. |
| `feature/auth/RegisterScreen.kt` | Compose UI for registration. |
| `feature/auth/AuthViewModel.kt` | Handles login, registration, current-user loading, and logout. |
| `feature/auth/AuthUiState.kt` | Holds auth loading, errors, current user, and login state. |

Why it is separate:

- Auth has its own flow before the main app.
- The same ViewModel can support login, register, startup auth checks, and logout.

## Home Feature

| File | Purpose |
| --- | --- |
| `feature/home/HomeScreen.kt` | Compose UI for dashboard, progress, latest posts, continue-learning, and activity. |
| `feature/home/HomeViewModel.kt` | Loads dashboard data from `DashboardRepository`. |
| `feature/home/HomeUiState.kt` | Holds dashboard loading, error, and content state. |

Why it is separate:

- Home combines several summaries but should not own the logic for each backend area.

## Subjects Feature

| File | Purpose |
| --- | --- |
| `feature/subjects/SubjectsScreen.kt` | Compose UI for subject list and search. |
| `feature/subjects/SubjectsViewModel.kt` | Loads and filters subjects. |
| `feature/subjects/SubjectsUiState.kt` | Holds subject list state. |
| `feature/subjects/SubjectDetailScreen.kt` | Compose UI for modules, posts, and scans inside one subject. |
| `feature/subjects/SubjectDetailViewModel.kt` | Loads subject overview data. |
| `feature/subjects/SubjectDetailUiState.kt` | Holds subject detail state. |

Why it is separate:

- Subjects are a main navigation tab and a parent area for modules and scans.

## Modules Feature

| File | Purpose |
| --- | --- |
| `feature/modules/ModuleReaderScreen.kt` | Compose UI for reading module content and chapters. |
| `feature/modules/ModuleReaderViewModel.kt` | Loads module detail and chapters. |
| `feature/modules/ModuleReaderUiState.kt` | Holds module reader state. |

Why it is separate:

- Module reading is a detailed workflow with different layout needs than lists.

## Scans Feature

| File | Purpose |
| --- | --- |
| `feature/scans/BoardNotesScreen.kt` | Compose UI for board scan list. |
| `feature/scans/BoardNotesViewModel.kt` | Loads board scans and review-status filters. |
| `feature/scans/BoardNotesUiState.kt` | Holds board notes list state. |
| `feature/scans/OcrResultScreen.kt` | Compose UI for OCR text review and note saving. |
| `feature/scans/OcrResultViewModel.kt` | Loads scan detail and saves cleaned OCR text/status changes. |
| `feature/scans/OcrResultUiState.kt` | Holds OCR result editing state. |

Why it is separate:

- Board scan review has its own list and detail/editor flow.

## Study Tools Feature

| File | Purpose |
| --- | --- |
| `feature/studytools/AiSummaryScreen.kt` | Compose UI for AI summary result and follow-up actions. |
| `feature/studytools/AiSummaryViewModel.kt` | Requests a summary from the backend and exposes the result. |
| `feature/studytools/AiSummaryUiState.kt` | Holds summary loading, error, and content state. |
| `feature/studytools/FlashcardsScreen.kt` | Compose UI for reviewing generated flashcards. |
| `feature/studytools/FlashcardsViewModel.kt` | Requests generated flashcards from the backend. |
| `feature/studytools/FlashcardsUiState.kt` | Holds flashcard loading, error, and list state. |
| `feature/studytools/QuizScreen.kt` | Compose UI for generated quiz questions. |
| `feature/studytools/QuizViewModel.kt` | Requests a generated quiz from the backend. |
| `feature/studytools/QuizUiState.kt` | Holds quiz loading, error, and question state. |

Why it is separate:

- Study tools share a source-based flow, but each tool needs its own state and UI.

## Profile Feature

| File | Purpose |
| --- | --- |
| `feature/profile/ProfileScreen.kt` | Compose UI for profile information, preferences placeholders, and logout. |

Why it is currently simple:

- Profile is mostly visual in this phase.
- Editable account settings can be added later without affecting the rest of the app.

## Shared UI Components

| File | Purpose |
| --- | --- |
| `ui/components/BottomNavigationBar.kt` | Shared bottom navigation UI. |
| `ui/components/StudyLensCard.kt` | Shared card wrapper for consistent card styling. |
| `ui/components/StudyLensTopBar.kt` | Shared top bar for page titles, back buttons, and actions. |
| `ui/components/ProgressBar.kt` | Shared progress display. |
| `ui/components/ScreenState.kt` | Shared loading, error, empty, inline error, and refreshing states. |
| `ui/components/SectionHeader.kt` | Shared section title row. |
| `ui/components/StatusChip.kt` | Shared pill/chip for status and category labels. |

Why these components are used:

- They reduce repeated UI code.
- They keep screen styling consistent.
- They make feature screens easier to read.

## Navigation

| File | Purpose |
| --- | --- |
| `ui/navigation/AppRoutes.kt` | Stores route constants and route builder functions. |
| `ui/navigation/AppNavGraph.kt` | Connects routes to screens and creates ViewModels. |
| `ui/navigation/BottomNavItem.kt` | Defines bottom navigation items. |

Why navigation is centralized:

- Screens receive simple callbacks.
- Route strings are not duplicated across features.
- Bottom navigation visibility is handled in one place.

## Theme

| File | Purpose |
| --- | --- |
| `ui/theme/Color.kt` | StudyLens color tokens. |
| `ui/theme/Shape.kt` | Shared Material shape values. |
| `ui/theme/Type.kt` | Shared typography styles. |
| `ui/theme/Theme.kt` | Applies Material 3 color, shape, and type settings. |

Why theme files are used:

- Colors, typography, and shapes should not be redefined in every screen.
- The app can change visual direction later from one theme layer.
