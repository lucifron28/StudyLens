# Navigation

Navigation is handled with Navigation Compose.

## Main Files

```text
ui/navigation/AppRoutes.kt
ui/navigation/AppNavGraph.kt
ui/navigation/BottomNavItem.kt
ui/components/BottomNavigationBar.kt
```

## AppRoutes

`AppRoutes.kt` stores route constants and route builder functions.

Current routes:

```text
login
register
home
subjects
subjectDetail/{subjectId}
moduleReader/{moduleId}
scans
ocrResult/{scanId}
aiSummary/{sourceType}/{sourceId}
flashcards/{sourceType}/{sourceId}
quiz/{sourceType}/{sourceId}
profile
```

Why route builders are used:

```kotlin
fun createSubjectDetailRoute(subjectId: String) = "subjectDetail/$subjectId"
```

They prevent manually building route strings in multiple screens.

## AppNavGraph

`AppNavGraph.kt` connects each route to a screen.

Responsibilities:

- decide start destination from saved token
- show or hide bottom navigation
- create ViewModels with repositories
- read route arguments
- pass navigation callbacks into screens

## Bottom Navigation

Main tabs:

- Home
- Subjects
- Scans
- Profile

Bottom navigation is shown only for top-level routes.

Why:

- detail screens need more reading space
- back navigation should be clear on deeper screens

## Route Argument Examples

Subject detail:

```text
subjectDetail/{subjectId}
```

Module reader:

```text
moduleReader/{moduleId}
```

AI summary:

```text
aiSummary/{sourceType}/{sourceId}
```

This lets one AI summary screen work for multiple source types, such as module, chapter, or board scan.

## Navigation Direction

Examples:

```text
Subjects -> Subject Detail -> Module Reader -> AI Summary
Scans -> OCR Result -> AI Summary -> Flashcards or Quiz
Home -> Module Reader
Profile -> Logout -> Login
```

Screens do not directly own `NavController`. They receive simple callbacks from `AppNavGraph`.

Why:

- screens are easier to preview and test
- navigation logic stays centralized
