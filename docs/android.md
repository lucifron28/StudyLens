# Android Guide

The Android app is located in `app/` and uses Kotlin with Jetpack Compose.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Gson converter
- OkHttp logging interceptor
- DataStore Preferences
- Coil

## Package

```text
com.example.modulelensmobile
```

## App Structure

```text
app/src/main/java/com/example/modulelensmobile/
  MainActivity.kt
  ModuleLensApp.kt
  core/
  data/
  domain/
  feature/
  ui/
```

## core

Shared technical helpers.

```text
core/datastore/TokenManager.kt
core/format/TextFormatters.kt
core/network/AppContainer.kt
core/network/AuthInterceptor.kt
core/network/RetrofitClient.kt
core/network/TokenRefreshAuthenticator.kt
core/viewmodel/ViewModelFactory.kt
```

Important responsibilities:

- store JWT tokens in DataStore
- create Retrofit and OkHttp
- attach auth headers
- refresh expired tokens
- provide repositories and APIs through `AppContainer`
- share small formatting helpers
- create ViewModels without one factory class per screen

## data

Backend communication and mapping.

```text
data/remote/api/
data/remote/dto/
data/remote/ApiResult.kt
data/repository/
```

Rules:

- Retrofit interfaces only describe HTTP calls.
- DTOs match backend JSON.
- Repositories map DTOs into domain models.
- Screens should not directly use DTOs.
- API errors should use shared `apiResult()` or `toResult()` helpers.

## domain

Plain app models used by UI and repositories.

Examples:

- `User`
- `Subject`
- `LearningModule`
- `BoardScan`
- `Summary`
- `Flashcard`
- `Quiz`

## feature

Screen-level code.

Current feature folders:

```text
feature/auth/
feature/home/
feature/subjects/
feature/modules/
feature/scans/
feature/studytools/
feature/profile/
```

Usual pattern:

- `Screen.kt`: Compose UI
- `ViewModel.kt`: state loading and user actions
- `UiState.kt`: immutable state model for the screen

## ui

Shared app UI.

```text
ui/components/
ui/navigation/
ui/theme/
```

Reusable components:

- `BottomNavigationBar`
- `ModuleLensCard`
- `ModuleLensTopBar`
- `ProgressBar`
- `ScreenState`
- `SectionHeader`
- `StatusChip`

Shared state components in `ScreenState.kt`:

- `ModuleLensLoadingState`
- `ModuleLensErrorState`
- `ModuleLensInlineError`
- `ModuleLensEmptyState`
- `ModuleLensRefreshingIndicator`

## Navigation

Routes are defined in `AppRoutes.kt`.

```text
login
register
home
subjects
subjectDetail/{subjectId}
moduleReader/{moduleId}
scans
ocrResult/{scanId}
aiSummary/{sourceType}/{sourceId}
flashcards/{sourceType}/{sourceId}
quiz/{sourceType}/{sourceId}
profile
```

`AppNavGraph.kt` connects routes to screens and creates ViewModels with the shared generic factory.

Bottom navigation tabs:

- Home
- Subjects
- Scans
- Profile

## Android API Wiring

Currently wired backend areas:

- register
- login
- refresh token
- current user
- dashboard
- subjects
- subject overview
- module detail
- chapters
- board scans
- board scan update
- AI summary
- AI flashcards
- AI quiz

## Auth Storage

`TokenManager` stores:

- access token
- refresh token

`AuthInterceptor` attaches access tokens to requests. `TokenRefreshAuthenticator` attempts refresh when needed.

## Styling Rules

- Use `MaterialTheme`.
- Keep color tokens in `ui/theme/Color.kt`.
- Keep typography in `ui/theme/Type.kt`.
- Use shared components instead of duplicating cards, loading screens, and error screens.
- Keep feature screens focused on UI composition.
