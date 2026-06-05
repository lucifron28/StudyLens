# ModuleLens

ModuleLens is a student-only course learning companion. The project has a Django REST Framework backend and a native Android app built with Kotlin and Jetpack Compose.

The main goal is to let a student read course modules, save classroom whiteboard OCR notes, attach those notes to subjects/modules/chapters, generate study materials, and use an AI tutor mode where the student must answer clearly before a topic is considered mastered.

This repository currently contains:

- A Dockerized Django REST Framework backend in `backend/`
- PostgreSQL running through Docker Compose
- JWT authentication for the Android app
- Media upload support for module files and board scan images
- Swagger/OpenAPI documentation
- Study tool models and endpoints
- Swappable local AI service layer using Ollama first
- Native Android app scaffold with Compose theme, navigation, auth flow, and dashboard wiring
- Google Stitch screenshots used as UI reference material under `Docs/prototype/`

## Current Status

Backend status:

- Phase 1 and Phase 2 backend are implemented.
- Phase 3 study tools are implemented.
- Phase 4 AI service layer is implemented.
- PostgreSQL, migrations, seed data, admin registration, Swagger docs, and user-owned filtering are in place.
- Local AI is configured for Ollama using `qwen3:4b-instruct`.
- Gemini is intentionally left as a placeholder until an API key and provider implementation are needed.

Android status:

- The Android project is named `ModuleLensMobile`.
- Package namespace is `com.example.modulelensmobile`.
- Compose theme, reusable UI components, bottom navigation, and main routes are in place.
- Login/register use the backend JWT auth endpoints.
- Tokens are stored with DataStore.
- Retrofit uses `http://10.0.2.2:8000/` for Android emulator access to the local backend.
- Home dashboard is wired to the backend dashboard endpoint.
- Several screens still use UI-first or mock data while endpoint wiring continues.

## Repository Structure

```text
ModuleLens/
  README.md
  app/
    src/main/java/com/example/modulelensmobile/
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

## Backend Tech Stack

- Python 3.12 in Docker
- Django 5
- Django REST Framework
- PostgreSQL
- djangorestframework-simplejwt
- django-cors-headers
- drf-spectacular
- Pillow
- httpx
- psycopg binary package

## Android Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Gson converter
- OkHttp logging interceptor
- DataStore Preferences
- Coil

## Backend Setup

Run these commands from the backend folder:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
Copy-Item .env.example .env
docker compose build
docker compose up -d db
docker compose run --rm web python manage.py migrate
docker compose up web
```

Alternative command to run everything in the background:

```powershell
docker compose up -d --build
```

If you change environment variables in `.env`, recreate the web container so Docker picks up the new values:

```powershell
docker compose up -d --force-recreate web
```

Open Swagger docs:

```text
http://localhost:8000/api/docs/
```

Open Django admin:

```text
http://localhost:8000/admin/
```

Create a superuser:

```powershell
docker compose run --rm web python manage.py createsuperuser
```

Seed demo data:

```powershell
docker compose run --rm web python manage.py seed_demo_data
```

Demo account from the seed command:

```text
username: demo_student
password: demo-password-123
```

## Backend Environment Variables

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
- Native Android does not require CORS, but CORS is useful for browser-based tools.
- Do not put real secrets in `.env.example`.
- Do not expose Gemini or other AI keys to the Android app.

## Android Setup

Open the repository root in Android Studio:

```text
C:\Users\Ron\Documents\projects\ModuleLens
```

Android Studio should detect the Gradle project with:

```text
rootProject.name = "ModuleLensMobile"
include(":app")
```

Run the app on an emulator after the backend is running.

The Android emulator reaches the host machine backend through:

```text
http://10.0.2.2:8000/
```

The current Retrofit base URL is set in:

```text
app/src/main/java/com/example/modulelensmobile/core/network/RetrofitClient.kt
```

For a physical Android phone, use your laptop LAN IP instead:

```text
http://YOUR_LAPTOP_LAN_IP:8000/
```

Also add that LAN IP to `ALLOWED_HOSTS` in `backend/.env`, for example:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.1.20
```

Then recreate the backend web container:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose up -d --force-recreate web
```

## Android App Routes

Current routes:

- `login`
- `register`
- `home`
- `subjects`
- `subjectDetail/{subjectId}`
- `moduleReader/{moduleId}`
- `scans`
- `ocrResult`
- `aiSummary`
- `profile`

Bottom navigation tabs:

- Home
- Subjects
- Scans
- Profile

## Authentication Flow

The backend uses Django's default `User` model and JWT tokens.

Endpoints:

- `POST /api/auth/register/`
- `POST /api/auth/token/`
- `POST /api/auth/token/refresh/`
- `GET /api/auth/me/`

The Android app:

- Sends login/register requests through Retrofit.
- Stores access and refresh tokens in DataStore.
- Adds `Authorization: Bearer ACCESS_TOKEN` through an OkHttp interceptor.
- Attempts token refresh when the backend returns an authentication failure.
- Logs the user out by clearing saved tokens.

## Backend Data Ownership Rule

All student data belongs to `request.user`.

The backend filters querysets by the authenticated owner and sets `owner=request.user` during create operations. This applies to subjects, modules, chapters, tags, board scans, reading progress, tasks, posts, summaries, flashcards, quizzes, quiz attempts, tutor sessions, and tutor messages.

The first version is student-only. Teacher/admin roles are intentionally not implemented yet.

## Backend Models

Learning app:

- `Subject`
- `Module`
- `Chapter`
- `Tag`
- `BoardScan`
- `ReadingProgress`
- `AcademicTask`
- `SubjectPost`

Study tools app:

- `Summary`
- `Flashcard`
- `Quiz`
- `QuizQuestion`
- `QuizAttempt`

AI services app:

- `TutorSession`
- `TutorMessage`

## API Endpoint Summary

Authentication:

- `POST /api/auth/register/`
- `POST /api/auth/token/`
- `POST /api/auth/token/refresh/`
- `GET /api/auth/me/`

Learning:

- `GET /api/learning/dashboard/`
- `/api/learning/subjects/`
- `GET /api/learning/subjects/{id}/overview/`
- `/api/learning/modules/`
- `/api/learning/chapters/`
- `/api/learning/tags/`
- `/api/learning/board-scans/`
- `/api/learning/reading-progress/`
- `POST /api/learning/reading-progress/set/`
- `/api/learning/tasks/`
- `/api/learning/posts/`

Study tools:

- `/api/studytools/summaries/`
- `/api/studytools/flashcards/`
- `/api/studytools/quizzes/`
- `/api/studytools/quiz-questions/`
- `/api/studytools/quiz-attempts/`

AI:

- `POST /api/ai/summarize/`
- `POST /api/ai/generate-flashcards/`
- `POST /api/ai/generate-quiz/`
- `POST /api/ai/start-tutor/`
- `POST /api/ai/tutor-message/`
- `/api/ai/tutor-sessions/`
- `/api/ai/tutor-messages/`

Documentation:

- `GET /api/schema/`
- `GET /api/docs/`

## Common Query Parameters

Many list endpoints support search, ordering, and filters.

Examples:

- `?search=compose`
- `?ordering=title`
- `?ordering=-created_at`
- `?subject=1`
- `?module=1`
- `?chapter=1`
- `?content_type=pdf`
- `?review_status=needs_review`
- `?source_type=module`
- `?difficulty=easy`
- `?status=in_progress`
- `?task_type=deadline`
- `?priority=high`
- `?post_type=announcement`
- `?is_pinned=true`

Use Swagger at `/api/docs/` for exact request and response fields.

## Sample API Tests

Register a student:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/register/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\",\"first_name\":\"Student\"}"
```

Get JWT tokens with email:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/token/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\"}"
```

Get JWT tokens with username:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/token/ `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"student1\",\"password\":\"password123\"}"
```

Open the authenticated user profile:

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

Generate a summary from saved module text:

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

## Media Uploads

Module files:

- Field: `module_file`
- Upload path: `media/modules/user_<id>/`
- Current purpose: PDF upload support
- Future purpose: DOCX and PPTX support

Board scan images:

- Field: `image`
- Upload path: `media/board_scans/user_<id>/`
- Current purpose: whiteboard photo storage

OCR flow:

1. Android captures or selects a whiteboard image.
2. Android-side OCR extracts text, ideally through ML Kit in a future mobile phase.
3. Android sends `raw_ocr_text` and optional `cleaned_text` to the backend.
4. The backend stores the image, extracted text, review status, and optional subject/module/chapter links.

Advanced PDF, DOCX, and PPTX conversion is intentionally not implemented yet.

## Local AI Setup

The backend calls local Ollama from inside Docker:

```env
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
```

Make sure Ollama is running on the host machine and the model exists:

```powershell
ollama list
ollama pull qwen3:4b-instruct
```

If you change `OLLAMA_MODEL`, recreate the backend web container:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose up -d --force-recreate web
```

AI output safety:

- Summary generation accepts normal text output.
- Flashcard generation expects JSON array output.
- Quiz generation expects JSON object output.
- Tutor checks expect JSON object output with a clarity result.
- Invalid JSON returns `502 Bad Gateway` with a readable `detail` message.
- Very long source text is truncated before sending it to the local model.
- AI output is validated before saving.

## AI Tutor Rule

Tutor mode tracks clear student answers.

- A tutor session starts as `in_progress`.
- The AI asks the first question from module, chapter, or board scan context.
- The student answer is checked as `clear`, `partial`, or `unclear`.
- Clear answers increment `clear_answers_count`.
- When `clear_answers_count` reaches `target_clear_answers`, the session becomes `mastered`.
- The default target is 3 clear answers.

## Running Backend Checks

Run Django checks:

```powershell
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
docker compose exec -T web python manage.py check
```

Run migrations:

```powershell
docker compose exec -T web python manage.py migrate
```

Create migrations after model changes:

```powershell
docker compose exec -T web python manage.py makemigrations
```

Run tests when tests are available:

```powershell
docker compose exec -T web python manage.py test
```

## Running Android Checks

From the repository root:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Run instrumented tests from Android Studio or with a connected emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Common Troubleshooting

DisallowedHost for `10.0.2.2`:

- Make sure `10.0.2.2` is in `ALLOWED_HOSTS`.
- Recreate the Docker web container after changing `.env`.
- Confirm the backend setting with `python manage.py shell` if needed.

Android cannot connect to backend:

- Make sure Docker Desktop is running.
- Make sure `docker compose up web` or `docker compose up -d web` is running.
- Use `http://10.0.2.2:8000/` on the emulator, not `localhost`.
- Use the laptop LAN IP on a physical phone.
- Make sure Windows Firewall allows the connection for physical device testing.

AI endpoint timeout:

- Make sure Ollama is running.
- Make sure `qwen3:4b-instruct` is downloaded.
- Local model responses can be slow on first request.
- Keep source text short while testing.

Token or authentication issues:

- Register or log in again.
- Confirm the Android app saved the access token in DataStore.
- Use `/api/auth/me/` with the access token to verify the token is valid.

## Design References

Google Stitch screenshots are stored under:

```text
Docs/prototype/
```

Current reference screens:

- `home.png`
- `subjects.png`
- `subject-detail.png`
- `board-notes.png`
- `module-reader.png`
- `ai-summary.png`
- `ocr-result.png`
- `profile.png`

These screenshots guide the Android UI direction but are not strict one-to-one implementation requirements. The app should still follow the backend API structure.

## Future Work

Backend future work:

- Real PDF text extraction
- DOCX text extraction
- PPTX text extraction
- DOCX/PPTX to PDF conversion
- Full Gemini provider implementation
- More automated tests for AI endpoints with mocked providers
- Optional teacher/admin role in a later phase

Android future work:

- Wire subjects list to backend
- Wire subject detail to backend
- Wire module reader to backend
- Add board scan image capture and upload
- Add Android-side OCR with ML Kit
- Wire AI summary, flashcards, quiz, and tutor flows to backend
- Add loading, empty, and error states across all screens
- Add offline-friendly caching where useful

## Development Workflow

Recommended branch flow:

1. Start from updated `main`.
2. Create a focused branch for one feature or fix.
3. Implement and test only that scope.
4. Commit with a clear message.
5. Merge after review or after the feature is confirmed working.

Examples:

```powershell
git switch main
git pull
git switch -c codex/feature-name
git status
git add path/to/files
git commit -m "feat(scope): describe the change"
```

## Important Project Rules

- Keep the first version student-only.
- Do not call AI directly from Android.
- Do not expose API keys in the Android app.
- Keep backend code simple and explainable.
- Prefer standard DRF serializers, viewsets, routers, and permissions.
- Keep every protected query user-owned.
- Keep local development Docker-based.
- Treat Google Stitch screenshots as UI guidance, not as rigid requirements.
