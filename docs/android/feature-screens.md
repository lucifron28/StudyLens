# Feature Screens

This document explains each current feature folder.

## auth

Files:

- `LoginScreen.kt`
- `RegisterScreen.kt`
- `AuthViewModel.kt`
- `AuthUiState.kt`

Purpose:

- register a student
- log in with email/username and password
- save JWT tokens
- navigate to home after success

Why it is separate:

- authentication has different navigation behavior than logged-in screens
- login/register share one ViewModel and state shape

## home

Files:

- `HomeScreen.kt`
- `HomeViewModel.kt`
- `HomeUiState.kt`

Purpose:

- show dashboard progress
- show upcoming items
- show continue-learning modules
- show recent activity

Backend data:

- `GET /api/learning/dashboard/`

## subjects

Files:

- `SubjectsScreen.kt`
- `SubjectsViewModel.kt`
- `SubjectsUiState.kt`
- `SubjectDetailScreen.kt`
- `SubjectDetailViewModel.kt`
- `SubjectDetailUiState.kt`

Purpose:

- show subjects list
- search subjects
- open subject detail
- show modules, tasks, posts, and board scans for a subject

Backend data:

- `GET /api/learning/subjects/`
- `GET /api/learning/subjects/{id}/overview/`

## modules

Files:

- `ModuleReaderScreen.kt`
- `ModuleReaderViewModel.kt`
- `ModuleReaderUiState.kt`

Purpose:

- display module content
- show chapters
- provide entry point to AI summary generation

Backend data:

- `GET /api/learning/modules/{id}/`
- `GET /api/learning/chapters/?module={id}`

## scans

Files:

- `BoardNotesScreen.kt`
- `BoardNotesViewModel.kt`
- `BoardNotesUiState.kt`
- `OcrResultScreen.kt`
- `OcrResultViewModel.kt`
- `OcrResultUiState.kt`

Purpose:

- list saved board scans
- open OCR result detail
- edit cleaned OCR text
- update board scan review status/text
- navigate to AI summary for a board scan

Backend data:

- `GET /api/learning/board-scans/`
- `GET /api/learning/board-scans/{id}/`
- `PATCH /api/learning/board-scans/{id}/`

## studytools

Files:

- `AiSummaryScreen.kt`
- `AiSummaryViewModel.kt`
- `AiSummaryUiState.kt`
- `FlashcardsScreen.kt`
- `FlashcardsViewModel.kt`
- `FlashcardsUiState.kt`
- `QuizScreen.kt`
- `QuizViewModel.kt`
- `QuizUiState.kt`

Purpose:

- generate summaries
- show key takeaways
- generate flashcards from the same source
- generate quiz questions from the same source

Backend data:

- `POST /api/ai/summarize/`
- `POST /api/ai/generate-flashcards/`
- `POST /api/ai/generate-quiz/`

## profile

Files:

- `ProfileScreen.kt`

Purpose:

- show profile-style UI
- show app preferences placeholders
- provide logout action

Current limitation:

- the profile screen is mostly UI-focused and does not yet load editable account settings from the backend.
