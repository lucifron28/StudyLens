# Android Documentation

This directory explains the Android side of ModuleLens in more detail than the root README.

The Android app is a native Kotlin project using Jetpack Compose. It talks to the Django backend through Retrofit, stores JWT tokens with DataStore, and keeps UI code separated into feature screens, ViewModels, repositories, DTOs, domain models, and shared components.

## Guides

- [Architecture](architecture.md)
- [Project Structure](project-structure.md)
- [File Map](file-map.md)
- [Code Walkthrough](code-walkthrough.md)
- [Build and Dependencies](build-and-dependencies.md)
- [Networking and Authentication](networking-auth.md)
- [Data Layer](data-layer.md)
- [State and ViewModels](state-viewmodels.md)
- [Navigation](navigation.md)
- [UI, Theme, and Components](ui-theme-components.md)
- [Feature Screens](feature-screens.md)
- [How to Extend the Android App](extending.md)

## Quick Facts

- App module: `app`
- Package: `com.example.modulelensmobile`
- Root Gradle project name: `ModuleLensMobile`
- Minimum SDK: `24`
- Compile SDK: `37`
- Target SDK: `37`
- Local emulator backend URL: `http://10.0.2.2:8000/`

## Main Android Flow

```text
Compose Screen
  calls ViewModel actions
      |
      v
ViewModel
  owns UI state and launches repository calls
      |
      v
Repository
  calls Retrofit API and maps DTOs to domain models
      |
      v
Retrofit API
  sends HTTP requests to the Django backend
      |
      v
Django backend
```

## Current Screen Areas

- Authentication
- Home dashboard
- Subjects
- Subject detail
- Module reader
- Board notes
- OCR result editor
- AI summary
- Flashcards
- Quiz
- Profile
