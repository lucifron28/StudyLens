# StudyLens Backend

StudyLens is a student-only Django REST Framework backend for a native Android course module reader. It includes JWT authentication, PostgreSQL in Docker, subject/module/chapter management, board scan OCR note storage, reading progress, study tools, media uploads, CORS, admin, AI service endpoints, and Swagger/OpenAPI docs.

For the full project guide, including the Android app setup and development workflow, see `../README.md`.

DOCX/PPTX conversion is intentionally left for later phases.

## Tech Stack

- Python 3.12 in Docker
- Django + Django REST Framework
- PostgreSQL
- djangorestframework-simplejwt
- django-cors-headers
- drf-spectacular
- Pillow for uploaded board scan images
- httpx for local AI provider calls

## Setup

```powershell
cd <repo-root>\backend
Copy-Item .env.example .env
docker compose build
docker compose up -d db
docker compose run --rm web python manage.py migrate
docker compose up web
```

Open the API docs:

```text
http://localhost:8000/api/docs/
```

Open the Django admin:

```text
http://localhost:8000/admin/
```

Create a superuser:

```powershell
docker compose run --rm web python manage.py createsuperuser
```

Optional demo data:

```powershell
docker compose run --rm web python manage.py seed_demo_data
```

Demo account created by the seed command:

```text
username: demo_student
password: demo-password-123
```

## Environment Variables

Copy `.env.example` to `.env` and adjust values as needed.

```env
SECRET_KEY=dev-secret-key
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2
DATABASE_NAME=studylens_db
DATABASE_USER=studylens_user
DATABASE_PASSWORD=studylens_password
DATABASE_HOST=db
DATABASE_PORT=5432
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
GEMINI_API_KEY=
GEMINI_MODEL=
```

Default AI behavior uses Ollama through the backend. Gemini is present as a placeholder provider and returns a clear backend error until implemented.

## API Summary

Authentication:

- `POST /api/auth/register/` accepts email/password and optional username
- `POST /api/auth/token/` accepts username/password or email/password
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

Docs:

- `GET /api/schema/`
- `GET /api/docs/`

Useful query parameters:

- `?search=compose`
- `?ordering=title`
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

## Sample API Test

Register:

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

Open a subject overview:

```powershell
curl.exe http://localhost:8000/api/learning/subjects/1/overview/ `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Create an academic task:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/tasks/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"title\":\"Finish module reading\",\"task_type\":\"reading\",\"priority\":\"medium\",\"status\":\"pending\"}"
```

Create a subject post:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/posts/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"title\":\"Quiz reminder\",\"content\":\"Review Kotlin basics before Friday.\",\"post_type\":\"reminder\",\"is_pinned\":true}"
```

Upsert reading progress:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/reading-progress/set/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module\":1,\"chapter\":2,\"progress_percentage\":65,\"last_position\":\"page-4\",\"status\":\"reading\"}"
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

If the AI returns invalid JSON for flashcards, quizzes, or tutor checks, the backend returns `502 Bad Gateway` with a helpful `detail` message instead of crashing.

## Android Connection Notes

Android emulator:

```text
http://10.0.2.2:8000/
```

Keep `10.0.2.2` in `ALLOWED_HOSTS` because Django receives emulator requests with `Host: 10.0.2.2:8000`.

Physical Android device:

```text
http://YOUR_LAPTOP_LAN_IP:8000/
```

For a physical phone, make sure the phone and laptop are on the same Wi-Fi network. Add the laptop LAN IP to `ALLOWED_HOSTS` in `.env`, for example:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.1.20
```

Native Android apps do not need CORS, but CORS is enabled for browser-based tools during development.

## Local Ollama Notes

The backend calls Ollama from inside Docker using:

```env
OLLAMA_BASE_URL=http://host.docker.internal:11434
```

Make sure Ollama is running on the host machine and the configured model exists:

```powershell
ollama pull qwen3:4b-instruct
```

If you use a different local model, update `OLLAMA_MODEL` in `.env`, then restart the Django container:

```powershell
docker compose restart web
```

## File Upload Notes

- `Module.module_file` stores PDFs now and can store DOCX/PPTX later.
- `Module.extracted_text` is ready for future PDF/DOCX/PPTX text extraction.
- `BoardScan.image` stores whiteboard photos.
- OCR is expected to run on Android using ML Kit, then Android sends `raw_ocr_text` and `cleaned_text` to the backend.
- AI-generated summaries, flashcards, quizzes, and tutor messages are saved in PostgreSQL and always belong to the authenticated student.

Future TODOs already marked in the code:

- PDF text extraction
- DOCX text extraction
- PPTX text extraction
- DOCX/PPTX to PDF conversion
- real Gemini generation provider
