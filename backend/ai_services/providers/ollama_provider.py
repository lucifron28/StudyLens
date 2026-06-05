import httpx

from ai_services.providers.base import AIProviderError, BaseAIProvider


class OllamaProvider(BaseAIProvider):
    def __init__(self, base_url: str, model: str, timeout: float = 120.0):
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.timeout = timeout

    def generate_text(self, messages: list[dict], system_prompt: str | None = None) -> str:
        ollama_messages = []
        if system_prompt:
            ollama_messages.append({"role": "system", "content": system_prompt})
        ollama_messages.extend(messages)

        try:
            response = httpx.post(
                f"{self.base_url}/api/chat",
                json={
                    "model": self.model,
                    "messages": ollama_messages,
                    "stream": False,
                },
                timeout=self.timeout,
            )
            response.raise_for_status()
        except httpx.HTTPError as exc:
            raise AIProviderError(f"Ollama request failed: {exc}") from exc

        data = response.json()
        content = data.get("message", {}).get("content")
        if not content:
            raise AIProviderError("Ollama returned an empty response.")
        return content

