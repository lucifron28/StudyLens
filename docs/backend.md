# Backend Guide

The backend is a Django REST Framework API located in `backend/`.

## Tech Stack

- Python 3.12 in Docker
- Django
- Django REST Framework
- PostgreSQL
- djangorestframework-simplejwt
- django-cors-headers
- drf-spectacular
- Pillow
- httpx
- openai (for DeepSeek integration)
- PyMuPDF (PDF extraction)
- python-docx (Word extraction)
- python-pptx (PowerPoint extraction)
- psycopg binary package

## Backend Structure

```text
backend/
  config/
  accounts/
  learning/
  studytools/
  ai_services/
  media/
```

## config

`config/settings.py` contains:

- installed apps
- PostgreSQL configuration from environment variables
- JWT authentication defaults
- DRF pagination and permission defaults
- CORS configuration
- media and static settings
- drf-spectacular configuration

`config/urls.py` connects:

- Django admin
- auth routes
- learning routes
- study tool routes
- AI routes
- OpenAPI schema
- Swagger UI

## accounts

Responsibilities:

- student registration
- JWT token obtain
- JWT token refresh
- current-user endpoint

Endpoints:

```text
POST /api/auth/register/
POST /api/auth/token/
POST /api/auth/token/refresh/
GET  /api/auth/me/
```

The project uses Django's default `User` model. This keeps the first version simple and easy to defend.

## learning

Main models:

- `Subject`
- `Module`
- `Chapter`
- `Tag`
- `BoardScan`
- `ReadingProgress`
- `AcademicTask`
- `SubjectPost`

Key behavior:

- all records are student-owned
- querysets are filtered by `request.user`
- create actions set `owner=request.user`
- module files and board scan images support media uploads
- reading progress can be upserted through a custom endpoint
- subject overview provides data shaped for the Android subject detail screen
- dashboard endpoint provides home screen data

## studytools

Main models:

- `Summary`
- `Flashcard`
- `Quiz`
- `QuizQuestion`
- `QuizAttempt`

These records can be created manually through normal API endpoints or created by AI endpoints.

## ai_services

Main models:

- `TutorSession`
- `TutorMessage`

Important files:

- `service.py`: source text resolution, provider selection, AI calls, JSON parsing, and save logic
- `prompts.py`: prompt templates
- `providers/base.py`: provider interface
- `providers/deepseek_provider.py`: DeepSeek implementation via OpenAI SDK (Default)
- `providers/ollama_provider.py`: local Ollama implementation

## Media Uploads

Module files:

- field: `module_file`
- upload folder: `media/modules/user_<id>/`
- current use: PDF/module file storage
- future use: DOCX/PPTX support and text extraction

Board scan images:

- field: `image`
- upload folder: `media/board_scans/user_<id>/`
- expected mobile flow: Android captures image, Android OCR extracts text, backend stores image and OCR text

## User-Owned Filtering

All student data is protected by ownership rules.

The backend should never expose another student's records. When adding new endpoints, follow this pattern:

- filter list querysets with `owner=request.user`
- validate nested objects against `request.user`
- set `owner` on create server-side
- avoid trusting owner IDs sent by the client

## Admin

Models are registered in Django admin for development inspection. Admin is not the same as a teacher role. Teacher/admin product features are intentionally out of scope for the first version.

## Document Extraction

The backend has implemented text extraction for several file types uploaded by students:

- **PDF**: Uses `PyMuPDF` for fast text extraction.
- **DOCX**: Uses `python-docx` to extract text from Word documents.
- **PPTX**: Uses `python-pptx` to extract text from PowerPoint slides.

When files are attached to a `Module`, the backend parses the text contents and makes them available to the AI tools (Summarize, Flashcards, Quiz, Tutor) to use as study materials.
