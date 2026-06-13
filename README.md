# ModuleLens

ModuleLens is a student-only course learning companion. The repository contains two main parts:

- A Django REST Framework backend in `backend/`
- A native Android app in `app/` built with Kotlin and Jetpack Compose

The app is designed for students who need one place to read course modules, keep OCR-based whiteboard notes, track reading progress, and generate study support such as summaries, flashcards, quizzes, and tutor sessions. The current implementation keeps the architecture simple enough to explain in a BSIT project defense while still separating responsibilities clearly.

## Table of Contents

- [Current Status](#current-status)
- [Repository Map](#repository-map)
- [High-Level Architecture](#high-level-architecture)
- [Backend Codebase](#backend-codebase)
- [Android Codebase](#android-codebase)
- [Authentication Flow](#authentication-flow)
- [Data Ownership Rule](#data-ownership-rule)
- [API Endpoint Summary](#api-endpoint-summary)
- [Environment Variables](#environment-variables)
- [Run the Backend](#run-the-backend)
- [Run the Android App](#run-the-android-app)
- [Local Ollama AI Setup](#local-ollama-ai-setup)
- [Sample API Requests](#sample-api-requests)
- [Testing and Checks](#testing-and-checks)
- [Development Conventions](#development-conventions)
- [How to Extend the Project](#how-to-extend-the-project)
- [Documentation and Prototype Assets](#documentation-and-prototype-assets)
- [Known Future Work](#known-future-work)

## Current Status

Backend:

- Dockerized Django REST Framework backend is implemented.
- PostgreSQL runs through Docker Compose.
- JWT authentication is implemented with Simple JWT.
- CORS, media uploads, Swagger/OpenAPI docs, pagination, filtering, and admin registration are configured.
- Learning models and endpoints are implemented.
- Study tools models and endpoints are implemented.
- AI service endpoints are implemented.
- Ollama is the active local AI provider.
- Gemini exists as a placeholder provider and is not active because no API key is currently configured.
- First version is student-only. Teacher/admin roles are intentionally not implemented yet.

Android:

- Kotlin and Jetpack Compose app is implemented under `app/`.
- App package is `com.example.modulelensmobile`.
- Gradle root project name is `ModuleLensMobile`.
- Material 3 theme, reusable components, bottom navigation, and screen routes are in place.
- Login/register connect to backend JWT endpoints.
- Tokens are stored locally with DataStore.
- Retrofit uses `BuildConfig.API_BASE_URL`.
- Emulator default API URL is `http://10.0.2.2:8000/`.
- Home, subjects, subject detail, module reader, board notes, OCR result, AI summary, and profile screens are implemented.
- Android currently calls the backend for auth, dashboard, subjects, subject overview, modules, chapters, board scans, board scan updates, and AI summary generation.
- Backend support for flashcards, quizzes, and tutor mode exists, but the Android UI for those flows is future work.

## Repository Map

```text
ModuleLens/
  README.md
  settings.gradle.kts
  build.gradle.kts
  gradle/
    libs.versions.toml
  app/
    build.gradle.kts
    src/
      main/
        AndroidManifest.xml
        java/com/example/modulelensmobile/
          MainActivity.kt
          ModuleLensApp.kt
          core/
          data/
          domain/
          feature/
          ui/
  backend/
    Dockerfile
    docker-compose.yml
    .env.example
    requirements.txt
    manage.py
    config/
    accounts/
    learning/
    studytools/
    ai_services/
    media/
    README.md
  Docs/
    prototype/
```

Important note: `Docs/` is ignored by Git. It is used for local prototype screenshots, generated Word documents, and project documentation drafts that should not be included in implementation commits.

## High-Level Architecture

ModuleLens uses a straightforward client-server architecture.

```text
Android app
  Jetpack Compose screens
  ViewModels
  Repositories
  Retrofit APIs
  DataStore token storage
      |
      | HTTP JSON requests with JWT Bearer token
      v
Django REST Framework backend
  Accounts app
  Learning app
  Study tools app
  AI services app
  PostgreSQL database
  Media file storage
      |
      | Optional local AI requests
      v
Ollama running on the host machine
```

The Android app does not call AI services directly. All AI calls go through the backend so API keys, model configuration, prompt logic, validation, and saved results stay server-side.

## Backend Codebase

The backend is located in `backend/`.

### Backend Tech Stack

- Python 3.12 in Docker
- Django
- Django REST Framework
- PostgreSQL 16
- djangorestframework-simplejwt
- django-cors-headers
- drf-spectacular
- Pillow
- httpx
- psycopg binary package

### Backend Folder Responsibilities

```text
backend/
  config/
    settings.py        Django settings, installed apps, database, JWT, CORS, media, DRF, docs
    urls.py            Project URL routing and Swagger/OpenAPI endpoints
    wsgi.py            WSGI entrypoint
    asgi.py            ASGI entrypoint

  accounts/
    serializers.py     Register, login, and user serializers
    views.py           Register, token obtain, and current-user views
    urls.py            /api/auth/ routes
    permissions.py     Shared permission helpers if needed later

  learning/
    models.py          Subject, Module, Chapter, Tag, BoardScan, ReadingProgress, AcademicTask, SubjectPost
    serializers.py     DRF serializers and nested read-only display data
    views.py           ViewSets, dashboard endpoint, owner filtering, custom actions
    filters.py         Query parameter filtering
    urls.py            /api/learning/ router
    admin.py           Admin registration

  studytools/
    models.py          Summary, Flashcard, Quiz, QuizQuestion, QuizAttempt
    serializers.py     Study tool serializers
    views.py           Study tool ViewSets with owner filtering
    urls.py            /api/studytools/ router
    admin.py           Admin registration

  ai_services/
    models.py          TutorSession and TutorMessage
    serializers.py     AI request/response serializers and tutor serializers
    views.py           AI endpoints and tutor ViewSets
    service.py         Source text resolution, provider selection, JSON parsing, save logic
    prompts.py         Prompt templates
    providers/
      base.py          BaseAIProvider interface
      ollama_provider.py
      gemini_provider.py
    urls.py            /api/ai/ routes
    admin.py           Admin registration
```

### Backend Apps

`accounts`

- Uses Django's default `User` model.
- Provides student registration.
- Provides JWT token obtain and refresh endpoints.
- Provides `/api/auth/me/` for the Android app to load the current student.
- Token obtain accepts username/password or email/password.

`learning`

- Stores the main course-learning data.
- Handles subjects, modules, chapters, tags, board scans, reading progress, tasks, posts, and dashboard data.
- Supports media uploads for module files and board scan images.
- Filters all user-owned records by `request.user`.

`studytools`

- Stores generated or manual study assets.
- Includes summaries, flashcards, quizzes, quiz questions, and quiz attempts.
- The backend already supports these models even if the Android UI has not wired every flow yet.

`ai_services`

- Wraps AI generation behind backend endpoints.
- Selects the configured AI provider with `AI_PROVIDER`.
- Supports Ollama now.
- Contains a Gemini placeholder that fails clearly if not configured or implemented.
- Validates JSON output for flashcards, quizzes, and tutor checks before saving anything.

### Backend Data Models

Learning models:

- `Subject`
- `Module`
- `Chapter`
- `Tag`
- `BoardScan`
- `ReadingProgress`
- `AcademicTask`
- `SubjectPost`

Study tool models:

- `Summary`
- `Flashcard`
- `Quiz`
- `QuizQuestion`
- `QuizAttempt`

AI tutor models:

- `TutorSession`
- `TutorMessage`

### Backend Design Choices

- Standard DRF serializers, ViewSets, routers, filters, and permissions are preferred.
- Querysets are scoped to `request.user`.
- `perform_create()` sets owner fields automatically where needed.
- PostgreSQL is used in Docker for development.
- Media files are stored under backend media volumes.
- AI responses are never trusted blindly.
- Invalid AI JSON returns a controlled `502 Bad Gateway` response instead of crashing.
- DOCX/PPTX/PDF text extraction fields exist, but conversion is intentionally future work.

## Android Codebase

The Android app is located in `app/`.

### Android Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Gson converter
- OkHttp logging interceptor
- DataStore Preferences
- Coil

### Android Folder Responsibilities

```text
app/src/main/java/com/example/modulelensmobile/
  MainActivity.kt
  ModuleLensApp.kt

  core/
    datastore/
      TokenManager.kt
    format/
      TextFormatters.kt
    network/
      AppContainer.kt
      AuthInterceptor.kt
      RetrofitClient.kt
      TokenRefreshAuthenticator.kt
    viewmodel/
      ViewModelFactory.kt

  data/
    remote/
      ApiResult.kt
      api/
      dto/
    repository/

  domain/
    model/

  feature/
    auth/
    home/
    subjects/
    modules/
    scans/
    studytools/
    profile/

  ui/
    components/
    navigation/
    theme/
```

### Android Layers

`core`

- Contains app-wide technical helpers.
- `TokenManager` stores JWT access and refresh tokens in DataStore.
- `RetrofitClient` builds Retrofit with JSON conversion, auth headers, token refresh, and debug logging.
- `AppContainer` provides a simple dependency container for APIs and repositories.
- `TextFormatters` centralizes simple display formatting.
- `ViewModelFactory` removes the need for one factory file per ViewModel.

`data`

- Contains backend API interfaces, DTOs, API result helpers, and repositories.
- Retrofit interfaces define the exact backend routes the Android app calls.
- DTOs match backend JSON responses.
- Repositories map DTOs into domain models used by Compose screens.
- `ApiResult.kt` keeps API response error handling consistent.

`domain`

- Contains plain Kotlin models used by UI and repositories.
- These models keep screens independent from raw backend DTO names.

`feature`

- Organized by screen or workflow.
- Each major screen normally has:
  - a `Screen.kt` file for Compose UI
  - a `ViewModel.kt` file for data loading and actions
  - a `UiState.kt` file for state shape

`ui`

- Contains shared Compose components, navigation setup, bottom navigation items, routes, colors, typography, and theme.

### Android Screen Routes

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
profile
```

Bottom navigation routes:

- Home
- Subjects
- Scans
- Profile

### Android API Wiring

The Android app currently calls these backend areas:

- Auth:
  - register
  - login
  - refresh token
  - current user
- Learning:
  - dashboard
  - subjects list
  - subject overview
  - board scans list
  - board scan detail
  - board scan update
  - module detail
  - module chapters
- AI:
  - summarize

The backend has more endpoints than the Android app currently uses. That is expected because backend phases for study tools and tutor mode were implemented before all mobile screens were wired.

### Android Base URL

The API base URL is defined in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
```

`RetrofitClient.kt` reads this value from `BuildConfig.API_BASE_URL`.

For Android emulator testing, keep:

```text
http://10.0.2.2:8000/
```

For physical device testing, replace it with the laptop LAN IP and add that IP to Django `ALLOWED_HOSTS`.

## Authentication Flow

1. Student opens the Android app.
2. `AppNavGraph` checks the saved access token from `TokenManager`.
3. If no token exists, the app starts at `login`.
4. Login/register requests go through `AuthRepository`.
5. Tokens are stored in DataStore.
6. `AuthInterceptor` attaches `Authorization: Bearer <access token>` to backend requests.
7. If the backend rejects an expired token, `TokenRefreshAuthenticator` attempts to refresh it.
8. Logout clears saved tokens and returns to `login`.

Backend auth routes:

```text
POST /api/auth/register/
POST /api/auth/token/
POST /api/auth/token/refresh/
GET  /api/auth/me/
```

## Data Ownership Rule

Every student-owned backend record must belong to the authenticated user.

The backend follows these rules:

- List endpoints return only records owned by `request.user`.
- Detail endpoints only allow access to records owned by `request.user`.
- Create endpoints set `owner=request.user` in backend code.
- Nested choices such as subject, module, chapter, and board scan are validated so users cannot attach their data to another user's records.

This applies to:

- subjects
- modules
- chapters
- tags
- board scans
- reading progress
- academic tasks
- subject posts
- summaries
- flashcards
- quizzes
- quiz attempts
- tutor sessions
- tutor messages

## API Endpoint Summary

Swagger UI:

```text
GET /api/docs/
GET /api/schema/
```

Authentication:

```text
POST /api/auth/register/
POST /api/auth/token/
POST /api/auth/token/refresh/
GET  /api/auth/me/
```

Learning:

```text
GET    /api/learning/dashboard/
GET    /api/learning/subjects/
POST   /api/learning/subjects/
GET    /api/learning/subjects/{id}/
PATCH  /api/learning/subjects/{id}/
DELETE /api/learning/subjects/{id}/
GET    /api/learning/subjects/{id}/overview/

/api/learning/modules/
/api/learning/chapters/
/api/learning/tags/
/api/learning/board-scans/
/api/learning/reading-progress/
POST /api/learning/reading-progress/set/
/api/learning/tasks/
/api/learning/posts/
```

Study tools:

```text
/api/studytools/summaries/
/api/studytools/flashcards/
/api/studytools/quizzes/
/api/studytools/quiz-questions/
/api/studytools/quiz-attempts/
```

AI:

```text
POST /api/ai/summarize/
POST /api/ai/generate-flashcards/
POST /api/ai/generate-quiz/
POST /api/ai/start-tutor/
POST /api/ai/tutor-message/
/api/ai/tutor-sessions/
/api/ai/tutor-messages/
```

Useful list query parameters:

```text
?search=compose
?ordering=title
?ordering=-created_at
?subject=1
?module=1
?chapter=1
?content_type=pdf
?review_status=needs_review
?source_type=module
?difficulty=easy
?status=in_progress
?task_type=deadline
?priority=high
?post_type=announcement
?is_pinned=true
```

Use `/api/docs/` as the source of truth for exact request and response fields.

## Environment Variables

Copy `backend/.env.example` to `backend/.env`.

```env
SECRET_KEY=dev-secret-key
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2
DATABASE_NAME=modulelens_db
DATABASE_USER=modulelens_user
DATABASE_PASSWORD=modulelens_password
DATABASE_HOST=db
DATABASE_PORT=5432
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
GEMINI_API_KEY=
GEMINI_MODEL=
```

Important notes:

- Keep `10.0.2.2` in `ALLOWED_HOSTS` for Android emulator testing.
- Native Android apps do not need CORS, but CORS is useful for browser tools during development.
- Do not put real secrets in `.env.example`.
- Do not expose AI API keys in the Android app.

## Run the Backend

Run these commands from the backend folder:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
Copy-Item .env.example .env
docker compose build
docker compose up -d db
docker compose run --rm web python manage.py migrate
docker compose up web
```

Run everything in the background:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose up -d --build
```

Recreate the web container after changing `.env`:

```powershell
docker compose up -d --force-recreate web
```

Create a superuser:

```powershell
docker compose run --rm web python manage.py createsuperuser
```

Seed demo data:

```powershell
docker compose run --rm web python manage.py seed_demo_data
```

Demo account created by the seed command:

```text
username: demo_student
password: demo-password-123
```

Open local tools:

```text
Swagger:      http://localhost:8000/api/docs/
OpenAPI JSON: http://localhost:8000/api/schema/
Admin:        http://localhost:8000/admin/
```

## Run the Android App

Open this folder in Android Studio:

```text
C:\Users\Ron\Documents\projects\ModuleLens
```

Android Studio should detect:

```text
rootProject.name = "ModuleLensMobile"
include(":app")
```

Before running the app:

1. Start Docker Desktop.
2. Start the backend.
3. Confirm Swagger opens at `http://localhost:8000/api/docs/`.
4. Run the Android app on an emulator.

For emulator testing:

```text
Android app base URL: http://10.0.2.2:8000/
Backend browser URL: http://localhost:8000/
```

For physical phone testing:

1. Find the laptop LAN IP, for example `192.168.1.20`.
2. Change `API_BASE_URL` in `app/build.gradle.kts`.
3. Add the LAN IP to `ALLOWED_HOSTS` in `backend/.env`.
4. Recreate the backend web container.
5. Make sure Windows Firewall allows the phone to connect to port `8000`.

Example physical-device backend host config:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.1.20
```

## Local Ollama AI Setup

The backend calls Ollama from inside Docker using:

```env
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
```

Make sure Ollama is running on the host machine:

```powershell
ollama list
ollama pull qwen3:4b-instruct
```

If you change the AI model:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose up -d --force-recreate web
```

AI output rules:

- Summaries can be normal text.
- Flashcards must be JSON arrays.
- Quizzes must be JSON objects.
- Tutor answer checks must return structured clarity data.
- The backend defensively parses and validates AI output.
- Invalid model JSON returns a helpful `502` response.
- Very long input text is truncated before sending it to the model.

## Sample API Requests

Register:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/register/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\",\"first_name\":\"Student\"}"
```

Get tokens with email:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/token/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\"}"
```

Get the current user:

```powershell
curl.exe http://localhost:8000/api/auth/me/ `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Create a subject:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/subjects/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"title\":\"Native Android Development\",\"description\":\"Kotlin and Jetpack Compose\",\"color\":\"#2563EB\"}"
```

List subjects:

```powershell
curl.exe http://localhost:8000/api/learning/subjects/ `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Open dashboard data:

```powershell
curl.exe http://localhost:8000/api/learning/dashboard/ `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Create a module:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/modules/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"title\":\"Kotlin Basics\",\"description\":\"Variables and functions\",\"content_type\":\"markdown\",\"markdown_content\":\"# Kotlin Basics\"}"
```

Create a board scan note without an image:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/board-scans/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"module\":1,\"raw_ocr_text\":\"ViewModel connects UI state with repository.\",\"cleaned_text\":\"ViewModel connects UI state with repository.\",\"review_status\":\"needs_review\"}"
```

Upsert reading progress:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/reading-progress/set/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module\":1,\"progress_percentage\":65,\"last_position\":\"page-4\",\"status\":\"reading\"}"
```

Generate a summary from module text:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/summarize/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module_id\":1}"
```

Generate flashcards from direct text:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/generate-flashcards/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"text\":\"Kotlin uses val for read-only values and var for mutable values.\",\"count\":3}"
```

Start a tutor session:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/start-tutor/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module_id\":1,\"title\":\"Kotlin Tutor\"}"
```

Send a tutor answer:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/tutor-message/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"session_id\":1,\"message\":\"val is read-only, while var can be reassigned.\"}"
```

## Testing and Checks

Backend checks:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose exec -T web python manage.py check
docker compose exec -T web python manage.py migrate
docker compose exec -T web python manage.py test
```

Create migrations after backend model changes:

```powershell
docker compose exec -T web python manage.py makemigrations
```

Android checks from the repository root:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:lintDebug
.\gradlew.bat testDebugUnitTest
```

Build a debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Run instrumented tests with a connected emulator or device:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Development Conventions

Backend:

- Keep models and serializers simple.
- Use DRF ViewSets and routers for CRUD endpoints.
- Add custom API views only when the behavior is not normal CRUD.
- Always filter student-owned records by `request.user`.
- Always set owner fields server-side.
- Prefer clear validation errors over hidden assumptions.
- Keep AI provider logic behind `ai_services/service.py`.
- Keep prompt text in `ai_services/prompts.py`.
- Do not expose AI provider keys or model details to Android.

Android:

- Keep screens in `feature/<feature-name>/`.
- Keep backend JSON DTOs in `data/remote/dto/`.
- Keep Retrofit routes in `data/remote/api/`.
- Keep API-to-domain mapping in repositories.
- Keep screens reading one `UiState` from one ViewModel.
- Use reusable components from `ui/components/`.
- Use colors, typography, and shapes from `ui/theme/`.
- Avoid adding local duplicate loading/error/empty components.
- Avoid hardcoded API URLs in Kotlin files; use `BuildConfig.API_BASE_URL`.

Git:

- Keep branches focused.
- Commit small batches.
- Avoid committing generated docs from `Docs/`.
- Do not commit `.env`, media uploads, Gradle caches, or local IDE files.

## How to Extend the Project

### Add a New Backend Model

1. Add the model in the correct app, usually `learning`, `studytools`, or `ai_services`.
2. Add ownership fields if the record belongs to a student.
3. Add serializer validation.
4. Add a ViewSet with user-owned filtering.
5. Register the route in the app's `urls.py`.
6. Register the model in Django admin.
7. Run `makemigrations` and `migrate`.
8. Confirm the endpoint appears in Swagger.
9. Add or update Android DTOs, API interface methods, repository methods, and screens if the mobile app needs it.

### Add a New Android Screen

1. Add a feature folder under `feature/`.
2. Create:
   - `FeatureScreen.kt`
   - `FeatureViewModel.kt`
   - `FeatureUiState.kt`
3. Add route constants and builders in `AppRoutes.kt`.
4. Add a `composable()` entry in `AppNavGraph.kt`.
5. Add repository calls if the screen needs backend data.
6. Reuse shared components from `ui/components/`.
7. Run `:app:compileDebugKotlin`.

### Add a New Android API Call

1. Add or update the Retrofit method in `data/remote/api/`.
2. Add DTOs in `data/remote/dto/`.
3. Add mapping logic in the correct repository.
4. Return domain models, not raw DTOs, to ViewModels.
5. Use `apiResult()` or `toResult()` for consistent errors.
6. Expose loading/error/success state through the ViewModel.

### Add a New AI Feature

1. Add a prompt in `ai_services/prompts.py`.
2. Add service logic in `ai_services/service.py`.
3. Resolve source text from module, chapter, board scan, or direct text.
4. Truncate long input safely.
5. Validate AI output before saving.
6. Return helpful errors for invalid model output.
7. Add Android endpoint wiring only after the backend contract is stable.

## Documentation and Prototype Assets

Prototype screenshots live in:

```text
Docs/prototype/
```

Current prototype files:

```text
home.png
subjects.png
subject-detail.png
board-notes.png
module-reader.png
ai-summary.png
ocr-result.png
profile.png
```

These screenshots are UI guidance from Google Stitch. They should guide spacing, navigation, and visual direction, but the Android app should still follow the backend API structure and current code architecture.

Generated Word/PDF submission files are local deliverables and should stay out of implementation commits because `Docs/` is ignored.

## Common Troubleshooting

Backend does not start:

- Make sure Docker Desktop is running.
- Run `docker compose ps` inside `backend/`.
- Rebuild with `docker compose up -d --build`.
- Check logs with `docker compose logs -f web`.

Database connection fails:

- Make sure the `db` container is healthy.
- Confirm `DATABASE_HOST=db` inside Docker.
- Run `docker compose up -d db`.

Android emulator cannot connect:

- Use `http://10.0.2.2:8000/`, not `localhost`.
- Make sure Django has `10.0.2.2` in `ALLOWED_HOSTS`.
- Confirm Swagger opens in the host browser at `http://localhost:8000/api/docs/`.

Physical phone cannot connect:

- Use the laptop LAN IP.
- Add the LAN IP to `ALLOWED_HOSTS`.
- Recreate the Django web container.
- Make sure phone and laptop are on the same network.
- Check Windows Firewall for port `8000`.

AI endpoint times out:

- Make sure Ollama is running.
- Make sure `qwen3:4b-instruct` is downloaded.
- First local model response can be slow.
- Try a shorter text input while testing.

Authentication fails:

- Log in again.
- Check that the Android app stored tokens in DataStore.
- Test the token manually with `/api/auth/me/`.
- If the refresh token is expired, clear app data and log in again.

## Known Future Work

Backend:

- Real PDF text extraction
- DOCX text extraction
- PPTX text extraction
- Optional DOCX/PPTX to PDF conversion
- Full Gemini provider implementation
- More tests for AI endpoints with mocked providers
- More tests for permissions and owner filtering
- Optional teacher/admin role in a later phase

Android:

- Add board scan camera/gallery upload flow
- Add Android-side OCR with ML Kit
- Wire flashcard generation UI
- Wire quiz generation and quiz attempt UI
- Wire AI tutor mode UI
- Add offline caching where useful
- Add richer profile/account settings
- Add better empty states for new accounts
- Add instrumented navigation tests

## Project Rules

- Keep the first version student-only.
- Do not implement teacher/admin role yet.
- Do not call AI directly from Android.
- Do not expose Gemini or other API keys in the Android app.
- Keep code readable and explainable.
- Prefer boring, standard DRF patterns.
- Prefer reusable Compose components over duplicated screen helpers.
- Keep media uploads and local AI development Docker-friendly.
- Keep generated documentation and prototype exports out of implementation commits.
