# Setup Guide

This guide explains how to run the backend and Android app locally.

## Requirements

- Docker Desktop
- Android Studio
- Android emulator or physical Android device
- Ollama for local AI features
- Git

## Backend Environment

Copy the example environment file:

```powershell
cd <repo-root>\backend
Copy-Item .env.example .env
```

Default development values:

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
AI_PROVIDER=deepseek
DEEPSEEK_API_KEY=your_key_here
DEEPSEEK_MODEL=deepseek-chat
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
GEMINI_API_KEY=
GEMINI_MODEL=
```

## Run Backend with Docker

```powershell
cd <repo-root>\backend
docker compose up -d --build
docker compose exec -T web python manage.py migrate
```

Open:

```text
http://localhost:8000/api/docs/
```

Create a superuser:

```powershell
docker compose run --rm web python manage.py createsuperuser
```

Seed demo data:

```powershell
docker compose run --rm web python manage.py seed_demo_data
```

Demo account:

```text
username: demo_student
password: demo-password-123
```

## Recreate Backend After Environment Changes

If `.env` changes:

```powershell
docker compose up -d --force-recreate web
```

## Run Android App

Open the repository root in Android Studio:

```text
<repo-root>
```

Start the backend first, then run the app on an emulator.

Emulator backend URL:

```text
http://10.0.2.2:8000/
```

This URL is configured in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
```

## Physical Device Testing

Use your laptop LAN IP instead of `10.0.2.2`.

Example:

```text
http://192.168.1.20:8000/
```

Then update backend `.env`:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.1.20
```

Recreate the web container:

```powershell
docker compose up -d --force-recreate web
```

Also check Windows Firewall if the phone cannot reach the backend.

## Local Ollama Setup

```powershell
ollama list
ollama pull qwen3:4b-instruct
```

Keep Ollama running while testing AI endpoints.
