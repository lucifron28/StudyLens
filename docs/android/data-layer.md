# Data Layer

The data layer is responsible for backend communication and converting backend JSON into app-friendly models.

## Main Folders

```text
data/remote/api/
data/remote/dto/
data/remote/ApiResult.kt
data/repository/
domain/model/
```

## Retrofit APIs

Retrofit APIs are interface files that describe backend routes.

Current API files:

- `AuthApi.kt`
- `LearningApi.kt`
- `AiApi.kt`

Why Retrofit interfaces are used:

- They make HTTP calls readable.
- Each function shows method, path, body, and query parameters.
- ViewModels do not need to know URL details.

## DTOs

DTO means data transfer object.

DTO files:

- `AuthDtos.kt`
- `DashboardDtos.kt`
- `SubjectDtos.kt`
- `ModuleDtos.kt`
- `BoardScanDtos.kt`
- `AiDtos.kt`

Why DTOs exist:

- Backend JSON field names may not be ideal for UI.
- DTOs can match the API exactly.
- Domain models can stay cleaner.

## Domain Models

Domain models are used by ViewModels and screens.

Examples:

- `Subject`
- `LearningModule`
- `BoardScan`
- `Summary`
- `Flashcard`
- `Quiz`
- `User`

Why domain models are used:

- Screens do not depend on backend response shape.
- UI names can be clearer than API field names.
- Mapping logic stays in repositories.

## Repositories

Repository files:

- `AuthRepository.kt`
- `DashboardRepository.kt`
- `SubjectsRepository.kt`
- `ModulesRepository.kt`
- `BoardScansRepository.kt`
- `AiRepository.kt`

Repository responsibilities:

- call Retrofit APIs
- handle response success/failure
- map DTOs to domain models
- provide simple functions to ViewModels

Example flow:

```text
HomeViewModel
  -> DashboardRepository.getDashboard()
  -> LearningApi.getDashboard()
  -> DashboardDto
  -> Dashboard domain model
  -> HomeUiState
```

## ApiResult

`ApiResult.kt` centralizes response handling.

It helps with:

- checking HTTP success
- handling empty response bodies
- converting DTOs into domain models
- returning readable error messages

Why:

- Avoids repeating the same `try/catch` and `response.isSuccessful` code.
- Keeps repository methods shorter and easier to read.

## Formatting Helpers

`TextFormatters.kt` contains shared helpers:

- `toDisplayLabel()`
- `toPreview()`
- `toReadableDate()`

Why:

- Avoids duplicated formatting functions across repositories and screens.
- Keeps labels like `needs_review` readable as `Needs Review`.
