# Troubleshooting

## Backend Does Not Start

Check Docker:

```powershell
docker compose ps
docker compose logs -f web
```

Rebuild:

```powershell
docker compose up -d --build
```

## Database Connection Fails

Make sure the database container is running:

```powershell
docker compose up -d db
```

Inside Docker, `DATABASE_HOST` should be:

```env
DATABASE_HOST=db
```

## Android Emulator Cannot Connect

Use:

```text
http://10.0.2.2:8000/
```

Do not use `localhost` from the emulator.

Also confirm:

- Docker backend is running
- Swagger opens on the host browser
- `10.0.2.2` is in backend `ALLOWED_HOSTS`

## Physical Phone Cannot Connect

Use the laptop LAN IP:

```text
http://192.168.1.20:8000/
```

Then update backend `ALLOWED_HOSTS`.

Also check:

- phone and laptop are on the same network
- Windows Firewall allows port `8000`
- backend container was recreated after `.env` changed

## DisallowedHost Error

Add the host to `.env`:

```env
ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.1.20
```

Recreate web:

```powershell
docker compose up -d --force-recreate web
```

## AI Errors / Timeouts

If using DeepSeek:

- Check your `.env` for a valid `DEEPSEEK_API_KEY`
- Verify that `AI_PROVIDER=deepseek`
- Make sure your Docker container has internet access
- Check if you have sufficient API credits

If using Ollama (local fallback):

```powershell
ollama list
```

Common fixes:

- make sure Ollama is running
- make sure `qwen3:4b-instruct` exists
- retry once because first model load can be slow
- test with shorter input text

## Authentication Fails

Try:

- log in again
- clear app data on emulator
- verify token with `/api/auth/me/`
- confirm the backend is using the same database you seeded

## Git Pull Is Blocked by Local Changes

Commit or stash first:

```powershell
git status
git add path/to/files
git commit -m "wip: save local changes"
git pull --rebase origin main
```

Resolve conflicts, then:

```powershell
git add resolved/file
git rebase --continue
```
