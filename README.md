# StudyLens

StudyLens is a student-only course learning companion with a Django REST Framework backend and a native Android app built with Kotlin and Jetpack Compose.

Students can read course modules, save classroom whiteboard OCR notes, review board scans, generate study materials, and use AI-assisted study flows. The Android app never calls AI providers directly; it talks to the backend, and the backend talks to the configured local AI provider.

## Project Parts

- `backend/`: Django REST Framework API, PostgreSQL, JWT auth, media uploads, Swagger docs, study tools, and AI service layer.
- `app/`: Android app using Kotlin, Jetpack Compose, Material 3, Retrofit, Navigation Compose, and DataStore.
- `docs/`: focused project documentation.

## Start Here

- [Project Overview](docs/overview.md)
- [Setup Guide](docs/setup.md)
- [Backend Guide](docs/backend.md)
- [Android Guide](docs/android/README.md)
- [API Guide](docs/api.md)
- [AI Service Guide](docs/ai.md)
- [Development Workflow](docs/development.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Future Work](docs/future-work.md)

## Quick Backend Run

```powershell
cd <repo-root>\backend
Copy-Item .env.example .env
docker compose up -d --build
docker compose exec -T web python manage.py migrate
```

Open:

```text
Swagger: http://localhost:8000/api/docs/
Admin:   http://localhost:8000/admin/
```

## Quick Android Run

Open the repository root in Android Studio:

```text
<repo-root>
```

Run the app on an emulator while the backend is running. The emulator uses:

```text
http://10.0.2.2:8000/
```

The API base URL is configured in `app/build.gradle.kts` through `BuildConfig.API_BASE_URL`.

## Current AI Model

Local development uses Ollama:

```env
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
```

Gemini is intentionally left as a placeholder until an API key and provider implementation are needed.

## Repository Rule

Keep generated files and prototype assets out of implementation commits. The tracked docs in `docs/*.md` are project documentation; prototype screenshots and Word/PDF exports remain ignored.
