from ai_services.providers.base import AIProviderError, BaseAIProvider


class GeminiProvider(BaseAIProvider):
    """
    Placeholder for a later Gemini implementation.

    The backend keeps Gemini swappable through AI_PROVIDER, but Phase 4 focuses on
    the local Ollama path so no Android app ever receives a Gemini API key.
    """

    def __init__(self, api_key: str, model: str):
        self.api_key = api_key
        self.model = model

    def generate_text(self, messages: list[dict], system_prompt: str | None = None) -> str:
        if not self.api_key:
            raise AIProviderError("Gemini is not configured. Set GEMINI_API_KEY on the backend.")
        raise AIProviderError("Gemini provider placeholder is present, but text generation is not implemented yet.")

