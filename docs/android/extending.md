# How to Extend the Android App

Use this guide when adding screens, API calls, or UI components.

## Add a New Screen

1. Create a feature folder:

```text
feature/newfeature/
```

2. Add three files:

```text
NewFeatureScreen.kt
NewFeatureViewModel.kt
NewFeatureUiState.kt
```

3. Add a route in `AppRoutes.kt`.

4. Add a `composable()` block in `AppNavGraph.kt`.

5. Inject needed repositories through `viewModelFactory`.

6. Reuse shared state components:

```kotlin
ModuleLensLoadingState(...)
ModuleLensErrorState(...)
ModuleLensEmptyState(...)
```

## Add a New API Call

1. Add a Retrofit method in `data/remote/api/`.
2. Add request/response DTOs in `data/remote/dto/`.
3. Add repository method in `data/repository/`.
4. Map DTOs to domain models.
5. Return `Result<T>`.
6. Call the repository from a ViewModel.
7. Expose result through a `UiState`.

## Add a New Domain Model

Place it in:

```text
domain/model/
```

Use domain models when a screen needs stable, UI-friendly data.

Avoid sending DTOs directly to screens.

## Add a New Shared Component

Place it in:

```text
ui/components/
```

Good candidates:

- repeated cards
- reusable list rows
- toolbar patterns
- empty states
- status labels

Do not create a shared component for one screen only unless it clearly improves readability.

## Add a New Bottom Navigation Tab

1. Add route to `AppRoutes.kt`.
2. Add item to `BottomNavItem.kt`.
3. Add route to the `bottomNavRoutes` list in `AppNavGraph.kt`.
4. Add the `composable()` destination.

## Add a New AI Screen

1. Add backend endpoint first.
2. Add Retrofit method in `AiApi.kt`.
3. Add DTOs in `AiDtos.kt`.
4. Add method in `AiRepository.kt`.
5. Map response to domain model.
6. Add feature screen, ViewModel, and UI state.

Keep in mind:

- Android should not build prompts.
- Android should not call Ollama or Gemini.
- Android should only send source IDs or text to the backend.

## Checklist Before Committing Android Changes

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:lintDebug
```

Also check:

- no duplicate loading/error/empty helpers
- no hardcoded backend URLs in Kotlin files
- no unused ViewModel factory classes
- no generated files committed
