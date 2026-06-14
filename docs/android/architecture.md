# Android Architecture

The Android app uses a simple layered architecture. The goal is to keep the code understandable while avoiding duplicated API calls, formatting, state handling, and UI components.

## Main Layers

```text
feature/
  Compose screens
  ViewModels
  UI state models

domain/
  Plain Kotlin models used by UI

data/
  Retrofit APIs
  DTOs
  Repositories

core/
  App-wide helpers
  Token storage
  Networking setup
  Formatting helpers

ui/
  Shared components
  Navigation
  Theme
```

## Why This Structure Is Used

The project uses these layers for practical reasons:

- Screens stay focused on UI.
- ViewModels handle loading and user actions.
- Repositories hide networking and mapping details.
- DTOs can match the backend JSON exactly.
- Domain models can stay friendly for the UI.
- Shared components prevent repeated loading, error, and empty states.
- The backend URL, token handling, and Retrofit setup stay in one place.

## Data Flow

Example: the subjects screen.

```text
SubjectsScreen
  displays SubjectsUiState
  calls viewModel.loadSubjects()

SubjectsViewModel
  sets isLoading
  calls SubjectsRepository.getSubjects()

SubjectsRepository
  calls LearningApi.getSubjects()
  maps SubjectDto to Subject

LearningApi
  sends GET /api/learning/subjects/

Backend
  returns only subjects owned by the logged-in user
```

## Error Flow

Most repository calls return `Result<T>`.

```text
Repository
  returns Result.success(data)
  or Result.failure(error)

ViewModel
  stores error.message in UiState

Screen
  shows StudyLensErrorState or StudyLensInlineError
```

This keeps exception handling out of screens.

## Authentication Flow

```text
LoginScreen/RegisterScreen
  -> AuthViewModel
  -> AuthRepository
  -> AuthApi
  -> backend JWT endpoint
  -> TokenManager saves tokens
```

After login, API requests use the saved access token automatically through `AuthInterceptor`.

## Why Android Does Not Call AI Directly

The app never talks directly to Ollama or Gemini. It sends study requests to the backend.

Reasons:

- AI keys and model configuration stay private.
- Prompt templates stay server-side.
- AI output can be validated before saving.
- Tutor mastery rules cannot be faked by changing local app state.
- The Android app only needs a stable backend contract.
