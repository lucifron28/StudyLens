# API Guide

The backend exposes JSON APIs under `/api/`.

Open Swagger:

```text
http://localhost:8000/api/docs/
```

Open raw OpenAPI schema:

```text
http://localhost:8000/api/schema/
```

## Authentication

```text
POST /api/auth/register/
POST /api/auth/token/
POST /api/auth/token/refresh/
GET  /api/auth/me/
```

## Learning

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
/api/learning/posts/
```

## Study Tools

```text
/api/studytools/summaries/
/api/studytools/flashcards/
/api/studytools/quizzes/
/api/studytools/quiz-questions/
/api/studytools/quiz-attempts/
```

## AI

```text
POST /api/ai/summarize/
POST /api/ai/generate-flashcards/
POST /api/ai/generate-quiz/
POST /api/ai/start-tutor/
POST /api/ai/tutor-message/
/api/ai/tutor-sessions/
/api/ai/tutor-messages/
```

## Common Query Parameters

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
?post_type=announcement
?is_pinned=true
```

## Sample Requests

Register:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/register/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\",\"first_name\":\"Student\"}"
```

Get tokens:

```powershell
curl.exe -X POST http://localhost:8000/api/auth/token/ `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"student1@example.com\",\"password\":\"password123\"}"
```

Get current user:

```powershell
curl.exe http://localhost:8000/api/auth/me/ `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Create subject:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/subjects/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"title\":\"Native Android Development\",\"description\":\"Kotlin and Jetpack Compose\",\"color\":\"#2563EB\"}"
```

Create module:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/modules/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"title\":\"Kotlin Basics\",\"description\":\"Variables and functions\",\"content_type\":\"markdown\",\"markdown_content\":\"# Kotlin Basics\"}"
```

Create board scan text note:

```powershell
curl.exe -X POST http://localhost:8000/api/learning/board-scans/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"subject\":1,\"module\":1,\"raw_ocr_text\":\"ViewModel connects UI state with repository.\",\"cleaned_text\":\"ViewModel connects UI state with repository.\",\"review_status\":\"needs_review\"}"
```

Generate summary:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/summarize/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module_id\":1}"
```

Generate flashcards:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/generate-flashcards/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"text\":\"Kotlin uses val for read-only values and var for mutable values.\",\"count\":3}"
```

Generate quiz:

```powershell
curl.exe -X POST http://localhost:8000/api/ai/generate-quiz/ `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d "{\"module_id\":1,\"count\":5}"
```
