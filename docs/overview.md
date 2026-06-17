# Project Overview

StudyLens is a student-only learning companion for course modules and classroom notes. It combines a Django REST Framework backend with a native Android app.

## Main Goal

The project helps students:

- read module and chapter content
- save board scan notes from classroom whiteboards
- attach notes to subjects, modules, and chapters
- track reading progress
- generate AI summaries
- generate flashcards and quizzes
- use an AI tutor mode that checks whether answers are clear

## Current Scope

The first version is student-only. Teacher, admin, or classroom-management roles are not part of the current scope.

Implemented backend areas:

- JWT authentication
- PostgreSQL in Docker
- user-owned subjects, modules, chapters, tags, board scans, reading progress, and posts
- study tool records for summaries, flashcards, quizzes, questions, and quiz attempts
- AI service layer for summaries, flashcards, quizzes, and tutor mode
- Swagger/OpenAPI docs
- media uploads for module files and board scan images

Implemented Android areas:

- auth screens
- token storage
- dashboard
- subjects list
- subject detail
- module reader
- board notes list
- OCR result editor
- AI summary screen
- flashcards screen
- quiz screen
- profile screen
- reusable theme, navigation, cards, state, and top/bottom bar components

## Architecture Summary

```text
Android app
  Compose screens
  ViewModels
  Repositories
  Retrofit APIs
  DataStore token storage
      |
      | HTTP JSON with JWT Bearer token
      v
Django REST Framework backend
  accounts
  learning
  studytools
  ai_services
  PostgreSQL
  media files
      |
      | optional local model calls
      v
Ollama on host machine
```

## Important Design Rules

- Android does not call AI providers directly.
- Backend owns all AI provider configuration.
- Every protected backend record is filtered by the authenticated user.
- The app is intentionally built with standard, readable patterns.
- The backend uses common DRF serializers, ViewSets, routers, filters, and permissions.
- The Android app uses simple Compose screens, ViewModels, repositories, DTOs, and domain models.

## Repository Layout

```text
StudyLens/
  README.md
  app/
  backend/
  docs/
  gradle/
```

`docs/` contains tracked Markdown documentation. Prototype images and generated submission files are also stored locally under `docs/`, but those assets are ignored by Git.
