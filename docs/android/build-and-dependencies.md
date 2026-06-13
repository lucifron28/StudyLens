# Build and Dependencies

This document explains the Android build setup and why each main dependency is used.

## SDK Configuration

Defined in `app/build.gradle.kts`:

```kotlin
compileSdk = 37

defaultConfig {
    minSdk = 24
    targetSdk = 37
}
```

Why:

- `minSdk = 24` gives a reasonable Android compatibility range.
- `compileSdk` and `targetSdk` are current for the installed Android SDK.

## Java Compatibility

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

Why:

- Java 11 is stable and common for Android projects.
- It works well with the current Kotlin/Android Gradle setup.

## BuildConfig API URL

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
```

Why:

- Keeps the backend base URL out of normal Kotlin source files.
- Makes it easier to change for emulator, physical device, or release builds later.
- `10.0.2.2` lets the Android emulator reach the host computer.

## Dependency Catalog

Dependencies are declared in:

```text
gradle/libs.versions.toml
```

Why:

- Keeps versions in one file.
- Makes upgrades easier to review.
- Avoids scattered version strings in Gradle files.

## Main Dependencies

### Jetpack Compose

Used for all Android UI.

Why:

- Modern Android UI toolkit.
- UI is written in Kotlin.
- Works naturally with state-driven screens.
- Good fit for screen prototypes from Google Stitch/Figma-style layouts.

### Material 3

Used for buttons, text fields, app bars, cards, colors, typography, and layout patterns.

Why:

- Gives a consistent design system.
- Reduces custom styling code.
- Provides standard Android components.

### Navigation Compose

Used for route-based navigation.

Why:

- Works directly with Compose.
- Keeps app routes centralized.
- Supports route arguments like `subjectId`, `moduleId`, and `scanId`.

### Lifecycle ViewModel Compose

Used to connect Compose screens to ViewModels.

Why:

- ViewModels survive recomposition.
- Keeps loading and data state out of Composable functions.

### Retrofit

Used for HTTP API calls to the Django backend.

Why:

- Standard Android networking library.
- Clean interface-based API definitions.
- Works with suspend functions.

### Gson Converter

Used by Retrofit for JSON serialization/deserialization.

Why:

- Converts backend JSON responses into DTO data classes.
- Simple and enough for this project.

### OkHttp Logging Interceptor

Used for debug request/response logs.

Why:

- Helps during API debugging.
- Only logs full body in debug builds.

### DataStore Preferences

Used for token storage.

Why:

- Modern replacement for SharedPreferences.
- Exposes tokens as Flows.
- Works well with Compose state collection.

### Coil

Used for image loading.

Why:

- Compose-friendly image loader.
- Useful for board scan images and profile/module media later.
