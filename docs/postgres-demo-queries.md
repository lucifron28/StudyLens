# StudyLens PostgreSQL Demo Queries

This file contains quick `psql` commands and SQL queries that Ron can use during a demo to prove that the StudyLens backend is connected to PostgreSQL and saving real data.

## Open PostgreSQL

From the project root:

```bash
docker compose -f backend/docker-compose.yml exec db psql -U studylens_user -d studylens_db
```

If the local `.env` still uses the older ModuleLens database names:

```bash
docker compose -f backend/docker-compose.yml exec db psql -U modulelens_user -d modulelens_db
```

## Basic Database Check

Show all tables:

```sql
\dt
```

Show the current database and user:

```sql
SELECT current_database() AS database_name, current_user AS database_user;
```

Check that Django migrations were applied:

```sql
SELECT app, name, applied
FROM django_migrations
ORDER BY applied DESC
LIMIT 10;
```

## Find Ron's Account

```sql
SELECT id, username, email, first_name, last_name, date_joined
FROM auth_user
WHERE username ILIKE '%ron%'
   OR email ILIKE '%ron%'
   OR first_name ILIKE '%ron%'
   OR last_name ILIKE '%ron%';
```

If Ron's user ID is needed for the next queries, copy the `id` from the result.

## Count Ron's Study Data

Replace `1` with Ron's actual `auth_user.id`.

```sql
SELECT
    (SELECT COUNT(*) FROM learning_subject WHERE owner_id = 1) AS subjects,
    (SELECT COUNT(*) FROM learning_module WHERE owner_id = 1) AS modules,
    (SELECT COUNT(*) FROM learning_boardscan WHERE owner_id = 1) AS board_scans,
    (SELECT COUNT(*) FROM studytools_summary WHERE owner_id = 1) AS summaries,
    (SELECT COUNT(*) FROM studytools_flashcard WHERE owner_id = 1) AS flashcards,
    (SELECT COUNT(*) FROM studytools_quiz WHERE owner_id = 1) AS quizzes,
    (SELECT COUNT(*) FROM ai_services_tutorsession WHERE owner_id = 1) AS tutor_sessions;
```

## Show Subjects

```sql
SELECT id, title, description, created_at
FROM learning_subject
ORDER BY created_at DESC
LIMIT 10;
```

Show only Ron's subjects:

```sql
SELECT id, title, description, created_at
FROM learning_subject
WHERE owner_id = 1
ORDER BY created_at DESC;
```

## Show Modules

```sql
SELECT
    m.id,
    s.title AS subject,
    m.title,
    m.content_type,
    m.original_filename,
    m.created_at
FROM learning_module m
LEFT JOIN learning_subject s ON s.id = m.subject_id
ORDER BY m.created_at DESC
LIMIT 10;
```

Show modules with extracted text previews:

```sql
SELECT
    id,
    title,
    content_type,
    LEFT(COALESCE(extracted_text, markdown_content, ''), 120) AS text_preview
FROM learning_module
ORDER BY created_at DESC
LIMIT 5;
```

## Show Board Notes and OCR Text

```sql
SELECT
    b.id,
    s.title AS subject,
    m.title AS module,
    b.review_status,
    LEFT(COALESCE(b.cleaned_text, b.raw_ocr_text, ''), 160) AS note_preview,
    b.created_at
FROM learning_boardscan b
LEFT JOIN learning_subject s ON s.id = b.subject_id
LEFT JOIN learning_module m ON m.id = b.module_id
ORDER BY b.created_at DESC
LIMIT 10;
```

## Show AI Summaries

```sql
SELECT
    id,
    source_type,
    LEFT(content, 180) AS summary_preview,
    created_at
FROM studytools_summary
ORDER BY created_at DESC
LIMIT 10;
```

## Show Flashcards

```sql
SELECT
    id,
    question,
    answer,
    difficulty,
    source_type,
    created_at
FROM studytools_flashcard
ORDER BY created_at DESC
LIMIT 10;
```

## Show Quizzes and Questions

```sql
SELECT
    q.id,
    q.title,
    q.source_type,
    COUNT(qq.id) AS question_count,
    q.created_at
FROM studytools_quiz q
LEFT JOIN studytools_quizquestion qq ON qq.quiz_id = q.id
GROUP BY q.id
ORDER BY q.created_at DESC
LIMIT 10;
```

Show quiz questions:

```sql
SELECT
    quiz_id,
    question,
    question_type,
    correct_answer,
    explanation
FROM studytools_quizquestion
ORDER BY quiz_id DESC, "order" ASC
LIMIT 10;
```

## Show Tutor Sessions

```sql
SELECT
    id,
    title,
    status,
    clear_answers_count,
    target_clear_answers,
    created_at
FROM ai_services_tutorsession
ORDER BY created_at DESC
LIMIT 10;
```

Show tutor messages:

```sql
SELECT
    session_id,
    role,
    clarity_result,
    LEFT(content, 180) AS message_preview,
    created_at
FROM ai_services_tutormessage
ORDER BY created_at DESC
LIMIT 10;
```

## Demo Flow

1. Run `\dt` to show that PostgreSQL has the StudyLens tables.
2. Run the Ron account query to show the logged-in student account.
3. Run the subject/module queries to show course data.
4. Run the board note query to show OCR text saved from the Android app.
5. Run the summary, flashcard, quiz, and tutor queries to show AI study outputs saved in the backend.

Exit `psql`:

```sql
\q
```
