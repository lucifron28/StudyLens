# Android Project Structure

The Android app is inside the `app/` Gradle module.

## Root Files

```text
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
app/build.gradle.kts
app/src/main/AndroidManifest.xml
```

Purpose:

- `settings.gradle.kts`: names the Gradle project and includes the `app` module.
- `build.gradle.kts`: root Gradle configuration.
- `gradle/libs.versions.toml`: dependency and plugin versions.
- `app/build.gradle.kts`: Android plugin, SDK versions, dependencies, and `BuildConfig` values.
- `AndroidManifest.xml`: app entry point, permissions, app class, and theme.

## Main Package

```text
app/src/main/java/com/example/modulelensmobile/
```

Top-level files:

- `MainActivity.kt`: Android entry activity. Sets the Compose content.
- `ModuleLensApp.kt`: application class. Creates the shared `AppContainer`.

## core

```text
core/datastore/TokenManager.kt
core/format/TextFormatters.kt
core/network/AppContainer.kt
core/network/AuthInterceptor.kt
core/network/RetrofitClient.kt
core/network/TokenRefreshAuthenticator.kt
core/viewmodel/ViewModelFactory.kt
```

`core` contains app-wide utilities that should not belong to one feature screen.

## data

```text
data/remote/ApiResult.kt
data/remote/api/
data/remote/dto/
data/repository/
```

`data` handles backend communication.

## domain

```text
domain/model/
```

`domain` contains clean app models used by screens and ViewModels.

## feature

```text
feature/auth/
feature/home/
feature/modules/
feature/profile/
feature/scans/
feature/studytools/
feature/subjects/
```

Each feature folder usually contains:

- a Compose screen
- a ViewModel
- a UI state data class

## ui

```text
ui/components/
ui/navigation/
ui/theme/
```

`ui` contains reusable UI building blocks, navigation setup, and the app theme.
