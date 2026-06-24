# Development Workflow

This project is being built branch by branch. Keep changes focused and commits small.

## Git Flow

Start from updated `main`:

```powershell
git switch main
git pull origin main
```

Create a focused branch using the right prefix:

```powershell
git switch -c feature/short-description
```

| Prefix | Use for |
|---|---|
| `feature/` | New screens, endpoints, or user-facing capabilities |
| `fix/` | Bug fixes that are not urgent |
| `hotfix/` | Urgent fixes that need to go straight to `main` |
| `refactor/` | Code restructuring with no behavior change |
| `docs/` | Documentation-only changes |
| `chore/` | Dependency bumps, build config, tooling |
| `test/` | Adding or fixing unit/integration tests |

Commit in small batches:

```powershell
git status
git add path/to/files
git commit -m "feat(scope): describe change"
```

## Commit Style

Examples:

```text
feat(auth): wire login to backend
feat(subjects): load subject overview
refactor(android): centralize shared app helpers
docs: split codebase documentation
fix(scans): handle empty OCR text
```

## Backend Checks

```powershell
cd <repo-root>\backend
docker compose exec -T web python manage.py check
docker compose exec -T web python manage.py test
```

Create migrations:

```powershell
docker compose exec -T web python manage.py makemigrations
docker compose exec -T web python manage.py migrate
```

## Android Checks

From the repository root:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:lintDebug
.\gradlew.bat testDebugUnitTest
```

## Backend Conventions

- Use DRF serializers, ViewSets, routers, and permissions.
- Keep custom API views for behavior that is not normal CRUD.
- Filter all student-owned querysets by `request.user`.
- Set `owner` server-side.
- Validate related objects against the current user.
- Keep AI prompt text in `ai_services/prompts.py`.
- Keep AI provider logic in `ai_services/providers/`.

## Android Conventions

- Put screen code in `feature/<name>/`.
- Use `Screen.kt`, `ViewModel.kt`, and `UiState.kt` for feature screens.
- Put Retrofit interfaces in `data/remote/api/`.
- Put DTOs in `data/remote/dto/`.
- Put backend-to-domain mapping in repositories.
- Use domain models in UI state.
- Use shared components from `ui/components/`.
- Use theme tokens from `ui/theme/`.
- Do not duplicate loading, error, or empty state components.
- Do not hardcode API URLs in Kotlin files.

## Documentation Conventions

- Keep `README.md` short.
- Put detailed project docs in `docs/*.md`.
- Keep generated Word/PDF files ignored.
- Keep prototype screenshots ignored unless there is a specific reason to track them.
