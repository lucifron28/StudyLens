# StudyLens Architecture and Design Decisions

StudyLens is a student-only study companion with a native Android client and a Django REST Framework backend. This document explains the decisions behind the current implementation, its boundaries, and the intentionally deferred work.

[`architecture.d2`](architecture.d2) and [`generate_architecture.py`](generate_architecture.py) are the maintained diagram sources. Regenerate the visual diagram after changing either source; this document explains why the runtime components are arranged this way.

## 1. System Boundaries

```text
Android app
  Compose UI -> ViewModels -> Repositories -> Django REST API
                                      |              |
                                 Room/DataStore    PostgreSQL/media
                                      |              |
                                 CameraX/ML Kit   AI providers
```

The Android app owns interaction, local device work, and short-term offline access. The backend owns shared student data, authorization, file storage, AI prompts, AI output validation, and mastery rules. The mobile app never talks to an AI provider directly.

## 2. Android Application

### Layered Feature Structure

The Android project uses a practical layered structure rather than a large framework:

- `feature/`: Compose screens, ViewModels, and UI-state classes grouped by user feature.
- `domain/`: UI-friendly Kotlin models.
- `data/`: Retrofit APIs, network DTOs, Room entities/DAOs, and repositories.
- `core/`: token storage, networking, OCR helpers, file helpers, and formatting.
- `ui/`: shared components, navigation, and theme tokens.

**Decision:** Screens render state and send user actions to a ViewModel. Repositories hide transport and mapping details.

**Why:** This keeps a screen readable during a defense. UI code does not need to know JSON field names, authorization headers, or cache tables.

### Local Data: Room and DataStore

StudyLens uses two different local stores because they solve different problems:

| Store | Current responsibility | Reason |
| --- | --- | --- |
| Room | Cached subjects, modules, and board scans | Relational data with queryable lists and offline fallbacks |
| DataStore | JWT tokens and theme preference | Small asynchronous preferences that do not need relational queries |

Repositories fetch fresh data from the API first. Successful unfiltered list responses replace the corresponding Room cache. When the request fails, the repository returns cached data if it exists; otherwise the original error reaches the ViewModel. The cache is a convenience layer, not a second source of truth.

**Limit:** CRUD changes are sent to the backend immediately. There is no queued offline-write or conflict-resolution system yet.

### Authentication Lifecycle

1. A student signs in with an email or username and password.
2. Django returns an access token and a refresh token.
3. `TokenManager` saves both tokens in DataStore.
4. `AuthInterceptor` adds `Authorization: Bearer <access token>` to protected requests.
5. `TokenRefreshAuthenticator` requests a new access token after a `401` response, then retries the original request.
6. Logging out clears local tokens and returns the user to the sign-in flow.

**Decision:** Use JWT rather than a server-side browser session.

**Why:** Android can authenticate each API request without cookie handling, while the backend remains stateless between requests.

### Profile and Appearance

The profile screen reads and updates the current user through `/api/auth/me/`, uploads an optional avatar through `/api/auth/me/image`, and stores the selected light, dark, or system theme in DataStore.

**Decision:** Keep account identity on the backend and device appearance on the device.

**Why:** Names and avatars must follow the student across devices; a theme setting is personal to one installed app and does not need a server round trip.

## 3. Camera, OCR, and Board Scans

1. CameraX captures a board image.
2. The student can crop, pan, and pinch-zoom the captured image before extraction.
3. ML Kit recognizes text on-device from the cropped image.
4. Android sends the cleaned OCR text, selected subject/module links, and optionally the board image to the backend.
5. Django stores the `BoardScan` record and its optional media file under the current student.

**Decision:** Perform OCR on-device, but allow the image to be retained when the student chooses to upload it.

**Why:** On-device OCR gives quick feedback, reduces backend compute, and avoids requiring a cloud OCR service. Retaining an image is still useful when students need to review a difficult extraction, so it is a deliberate student-controlled trade-off rather than a claim that images never leave the device.

## 4. Modules and Document Processing

### Current Implementation

Module uploads support PDF, DOCX, and PPTX files up to the backend validation limit. The backend stores the original upload, records its original filename, and extracts usable text for AI features:

- PDF: PyMuPDF
- DOCX: `python-docx`
- PPTX: `python-pptx`

The Android reader uses the platform `PdfRenderer` for PDF modules and cached downloaded files. Markdown and text modules render directly in Compose.

**Decision:** Extract document text on the backend.

**Why:** Python parsing libraries are mature and keep document-parsing complexity out of the Android app. The extracted text also becomes a stable source for summaries, flashcards, quizzes, and tutor sessions.

### Deliberately Deferred Conversion

DOCX/PPTX-to-PDF conversion through LibreOffice is not part of the active backend extraction service in this repository. It is a planned compatibility improvement for rendering Office uploads in the Android PDF reader.

When implemented, it should run outside the request/response path through a background worker, preserve the original Office file, store the converted PDF separately, and expose conversion status to Android. This avoids making a user wait on a potentially slow office conversion request.

## 5. Backend and Data Ownership

The Docker Compose environment runs a Django web service and PostgreSQL. Django REST Framework exposes JWT-protected endpoints for accounts, learning records, study tools, and AI features. Swagger/OpenAPI documents the API for development.

PostgreSQL is the authoritative store for student-owned records. Module files, board images, and profile images use Django media storage. Every user-facing learning record is scoped to `request.user` before it is listed, read, updated, or deleted.

**Decision:** Enforce ownership in backend querysets and serializer validation, not only in Android UI.

**Why:** A client can be modified or a request can be replayed. The server must remain responsible for preventing one student from accessing another student's subjects, modules, scans, study tools, and tutor sessions.

## 6. AI Service Layer

Android calls StudyLens AI endpoints, never DeepSeek or Ollama directly. The backend resolves owned source text, truncates it to a safe limit, builds the prompt, calls the selected provider, validates structured output, and saves the result.

Current provider choices are:

- Ollama for local development, with `qwen3:4b-instruct` as the default configuration.
- DeepSeek when a backend-only API key and model are configured.

**Decision:** Keep provider selection and prompt text on the server.

**Why:** API keys remain out of the APK, prompt changes do not require an Android release, and malformed model JSON becomes a controlled backend error rather than an app crash.

### AI Caching and Tutor Completion

The Android client caches generated summaries, flashcards, quizzes, and tutor sessions to avoid repeating slow model calls during normal review. Source changes invalidate the relevant cached study-tool results.

Tutor completion is not a permanent chat thread. The backend counts clear answers, marks a session mastered when the target is met, and the client presents completion or retry actions. The tutor should guide a student who is stuck without simply revealing the answer.

## 7. Reliability and Operational Limits

- API loading, refreshing, empty, and error states are shared Compose components.
- Repositories return `Result<T>` so screens do not handle transport exceptions directly.
- AI provider failures and invalid AI JSON are returned as readable backend errors.
- Docker provides repeatable local PostgreSQL and Django environments.

The project does not yet include background task processing, push notifications, server-side OCR, offline mutation queues, conflict resolution, teacher roles, or production object storage. These are intentional future work rather than hidden assumptions.

## 8. Decision Summary

| Decision | Chosen approach | Main reason |
| --- | --- | --- |
| Mobile UI | Kotlin and Jetpack Compose | State-driven native Android UI |
| App structure | Feature, domain, data, core, UI layers | Readable separation without excessive abstraction |
| Auth | JWT with DataStore and token refresh | Mobile-friendly stateless authentication |
| Offline access | Room cache for selected read models | Faster repeat views and basic offline fallback |
| OCR | CameraX plus on-device ML Kit | Fast local recognition with lower backend load |
| Documents | Backend extraction with Python libraries | Better parsing ecosystem and AI-ready text |
| API | Django REST Framework | Clear serializers, viewsets, permissions, and admin tools |
| Main database | PostgreSQL in Docker Compose | Reliable relational source of truth |
| AI | Backend-managed provider interface | Key security, centralized prompts, validated output |
