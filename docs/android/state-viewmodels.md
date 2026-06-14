# State and ViewModels

The app uses ViewModels to keep UI state separate from Compose UI code.

## Pattern

Most feature folders follow this pattern:

```text
FeatureScreen.kt
FeatureViewModel.kt
FeatureUiState.kt
```

## Screen

The screen file contains Composable functions.

Responsibilities:

- display UI
- collect state from ViewModel
- call ViewModel functions on user actions
- navigate through callbacks passed from `AppNavGraph`

Screens should not:

- call Retrofit directly
- parse DTOs
- store tokens
- own backend logic

## ViewModel

The ViewModel owns state and actions.

Responsibilities:

- load data on init when needed
- call repositories
- update loading state
- update error state
- expose immutable `StateFlow`

Why:

- state survives recomposition
- business logic is easier to test
- screens stay simple

## UiState

UI state is usually a Kotlin data class.

Common fields:

```kotlin
data class ExampleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

Feature-specific state adds the screen data.

Examples:

- `HomeUiState`
- `SubjectsUiState`
- `ModuleReaderUiState`
- `AiSummaryUiState`
- `FlashcardsUiState`
- `QuizUiState`

## StateFlow

ViewModels expose state as `StateFlow`.

Compose screens collect it with:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

Why:

- state changes trigger recomposition
- state remains observable
- ViewModel controls updates

## Shared ViewModel Factory

`ViewModelFactory.kt` provides:

```kotlin
viewModelFactory { SomeViewModel(...) }
```

Why:

- avoids one factory class per ViewModel
- keeps `AppNavGraph` readable
- still allows ViewModels to receive repositories and route arguments

## Error Handling Pattern

Repository:

```text
returns Result<T>
```

ViewModel:

```text
sets data on success
sets errorMessage on failure
```

Screen:

```text
shows StudyLensErrorState or StudyLensInlineError
```

This keeps the app consistent across features.
