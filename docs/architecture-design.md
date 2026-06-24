# StudyLens: Architecture & Design Decisions

This document outlines the end-to-end architecture of **StudyLens**, a mobile-first study companion. It details the technical choices, data flows, and security rules that govern the system from the Android client to the Django backend and out to the AI providers.

---

## 1. Authentication Flow

**Design Choice:** Stateless JWT (JSON Web Tokens) Authentication.

### Backend (Django REST Framework)
We use `djangorestframework-simplejwt`. The backend does not keep track of active sessions in the database. Instead, it issues a short-lived `access_token` (e.g., 60 minutes) and a long-lived `refresh_token` (e.g., 7 days).
- **Why?** Stateless JWTs allow the backend to remain highly scalable. Any instance of the Django server can verify a token without needing to query a session database table.

### Android Client
- **Storage:** Tokens are stored securely in **Jetpack DataStore** (Preferences). 
  - **Why?** Unlike the older `SharedPreferences` which reads synchronously and can freeze the app (causing ANRs), DataStore uses Kotlin Coroutines and Flow to safely read/write asynchronously on background threads. It is also transactional, preventing data corruption.
- **Interceptors:** An **OkHttp Interceptor** (`AuthInterceptor`) automatically intercepts every outbound network request, reads the `access_token` from DataStore, and injects it into the `Authorization: Bearer <token>` header.
- **Auto-Refresh:** If the server returns a 401 Unauthorized (because the access token expired), a specialized `Authenticator` class automatically intercepts the failure, uses the refresh token to silently request a new access token, and retries the original request without interrupting the user's flow.

---

## 2. Core Features: Modules, Board Scans, and Extraction

StudyLens organizes knowledge hierarchically (`Modules` -> `Chapters` -> `Study Materials`). Handling documents and images gracefully is critical to the app's success.

### Board Scans (Camera & OCR)
**Design Choice:** Perform OCR (Optical Character Recognition) directly on the Android device, rather than the server.

1. **Capture:** Android uses **CameraX** to capture high-quality images of whiteboards or notes.
2. **Cropping:** A custom `ImageCropper` allows the user to frame the exact text they want, discarding background noise.
3. **Extraction:** **Google ML Kit** processes the cropped image entirely *on-device*. 
4. **Upload:** The Android app sends only the *extracted text* to the Django backend.

**Why On-Device OCR?** 
1. **Bandwidth:** Sending a few kilobytes of raw text to the server is magnitudes faster than uploading a 5MB high-resolution photo over a cellular network.
2. **Privacy:** Photos of classrooms, whiteboards, or private notes never leave the user's phone. Only the text is sent.
3. **Server Load:** It saves the Django backend from doing heavy, GPU/CPU intensive image processing, drastically reducing hosting costs.

### Document Uploads (PDF, DOCX, PPTX)
**Design Choice:** Perform document extraction on the Backend.

Unlike images, parsing complex files like PDFs and PowerPoints requires heavy libraries and substantial memory. 
- The Android app uploads the raw file to the Django backend.
- The backend's Extraction Pipeline uses native Python libraries (`PyMuPDF`, `python-docx`, `python-pptx`) to parse text.
- **Why?** It keeps the Android APK size incredibly small. Furthermore, Python has the richest ecosystem for document parsing, ensuring much higher accuracy than attempting to parse a `.docx` file natively in Kotlin. (Note: We explicitly avoided using `LibreOffice` to keep the Docker container lightweight and fast).

---

## 3. AI & LLM Integration (The Brain)

The AI engine generates Flashcards, Quizzes, Summaries, and powers the interactive Tutor Mode.

### The "Golden Rule" of AI Security
**Mobile Never Touches AI Directly.**
The Android application must **never** make HTTP requests to DeepSeek, Ollama, or any AI provider.

**Why?**
1. **API Key Security:** Embedding a DeepSeek API key in an Android APK guarantees it will be stolen via reverse engineering. Moving this to the backend keeps keys completely hidden.
2. **Prompt Engineering:** Prompts must be centralized. If we tweak the "Tutor Persona" prompt, we only deploy an update to the backend. If prompts were in the Android code, users would have to download an app update from the Google Play Store just to see the changes.
3. **JSON Validation:** LLMs are notorious for returning malformed JSON. The backend has strict parsing rules (`parse_json_output`) to catch and sanitize bad JSON, preventing app crashes before the payload ever reaches the Android client.

### Provider Factory Pattern
The backend uses a Factory pattern (`ai_services/providers/base.py`) to hot-swap LLMs without changing any core business logic:
- **DeepSeek V4:** The primary, lightning-fast cloud provider (interfaced via the standard OpenAI SDK).
- **Ollama:** A local, offline fallback (e.g., `qwen3:4b-instruct`) used for local development and testing to save on API costs and enable offline development.

---

## 4. The Data Layer (Android vs. Backend)

### Backend Database (PostgreSQL 16)
Postgres serves as the ultimate source of truth. It stores relational links between Users, Modules, Quizzes, and tracks the state of Tutor Sessions (e.g., `clear_answers_count`).

### Android Offline Caching (Room Database)
To ensure the app feels fast and responsive even on spotty campus Wi-Fi, Android utilizes a **Room Database**.
1. When a user fetches their Modules or Chapters, the Android `Repository` saves a copy in the local SQLite database.
2. The UI instantly reads from the local Room database (Cache-first approach).
3. The app silently fetches the latest data from the Django API in the background and updates the Room cache.
4. Because the UI is observing the Room database via Kotlin `Flow`, the UI updates automatically and reactively the moment new data arrives.

---

## Summary of the Tech Stack

| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Mobile UI** | Kotlin + Jetpack Compose | Declarative UI, state-driven, modern Android standard. |
| **Mobile DB** | Room + DataStore | Room for complex relational caching; DataStore for asynchronous, safe JWT storage. |
| **Backend API** | Django REST Framework | Rapid development, robust ORM, excellent admin panel. |
| **Database** | PostgreSQL 16 | ACID compliant, reliable, handles relational text data well. |
| **AI Models** | DeepSeek V4 & Ollama | Cost-effective, highly capable, easy to swap via standard API interfaces. |
| **Infrastructure** | Docker Compose | Ensures the backend environment is identical across dev, staging, and production. |
