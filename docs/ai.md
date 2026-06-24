# AI Service Guide

AI runs through the backend. Android does not call DeepSeek, Ollama, or any AI provider directly.

## Provider Design

The backend defines a swappable provider interface:

```python
class BaseAIProvider:
    def generate_text(self, messages: list[dict], system_prompt: str | None = None) -> str:
        ...
```

Current providers:

- `DeepSeekProvider`
- `OllamaProvider`

Provider selection is controlled by:

```env
AI_PROVIDER=ollama
```

Ollama is the default local-development provider. Set `AI_PROVIDER=deepseek` only when the backend has a configured DeepSeek key and model.

## DeepSeek

Optional cloud provider (requires a backend-only API key):

```env
DEEPSEEK_API_KEY=your_key_here
DEEPSEEK_MODEL=deepseek-chat
```

DeepSeek is accessed via the official `openai` SDK since it offers OpenAI API compatibility.

## Ollama

Fallback local model:

```env
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=qwen3:4b-instruct
```

Docker uses `host.docker.internal` so the backend container can reach Ollama running on the Windows host.



## AI Endpoints

```text
POST /api/ai/summarize/
POST /api/ai/generate-flashcards/
POST /api/ai/generate-quiz/
POST /api/ai/start-tutor/
POST /api/ai/tutor-message/
```

## Source Text Resolution

AI endpoints can receive source content from:

- `module_id`
- `chapter_id`
- `board_scan_id`
- direct `text`

The backend resolves the source, checks ownership, extracts usable text, truncates long input, and sends the safe prompt to the provider.

## JSON Validation

Some AI endpoints require structured JSON.

Flashcards:

- expected output: JSON array
- each item should contain question, answer, and difficulty

Quiz:

- expected output: JSON object
- should include title and questions

Tutor checks:

- expected output: JSON object
- clarity result must be `clear`, `partial`, or `unclear`

If the model returns invalid JSON, the backend returns a controlled `502 Bad Gateway` response with a readable message.

## Tutor Rule

Tutor sessions track clear answers.

- default `target_clear_answers` is 3
- clear answers increment `clear_answers_count`
- once the target is reached, the session status becomes `mastered`

This rule lives on the backend so Android cannot fake mastery by changing local state.
