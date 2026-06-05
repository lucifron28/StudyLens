# ModuleLens Backend

ModuleLens is a student-only Django REST Framework backend for a native Android course module reader. It includes JWT authentication, PostgreSQL in Docker, subject/module/chapter management, board scan OCR note storage, reading progress, study tools, media uploads, CORS, admin, AI service endpoints, and Swagger/OpenAPI docs.

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
cd C:\Users\Ron\Documents\projects\ModuleLens\backend
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
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0
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

Default AI behavior uses Ollama through the backend. Gemini is present as a placeholder provider and returns a clear backend error until implemented.

## API Summary

Authentication:

- `POST /api/auth/register/`
- `POST /api/auth/token/`
- `POST /api/auth/token/refresh/`
- `GET /api/auth/me/`

Learning:

- `/api/learning/subjects/`
- `/api/learning/modules/`
- `/api/learning/chapters/`
- `/api/learning/tags/`
- `/api/learning/board-scans/`
- `/api/learning/reading-progress/`

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

## Sample API Test

Register:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/register/ `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"student1\",\"email\":\"student1@example.com\",\"password\":\"password123\",\"first_name\":\"Student\"}"
```

Get JWT tokens:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/token/ `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"student1\",\"password\":\"password123\"}"
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

Physical Android device:

```text
http://YOUR_LAPTOP_LAN_IP:8000/
```

For a physical phone, make sure the phone and laptop are on the same Wi-Fi network. Add the laptop LAN IP to `ALLOWED_HOSTS` in `.env`, for example:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,192.168.1.20
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
