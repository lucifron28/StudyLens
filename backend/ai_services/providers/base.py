from abc import ABC, abstractmethod


class AIProviderError(Exception):
    """Raised when a configured AI provider cannot return usable text."""


class BaseAIProvider(ABC):
    @abstractmethod
    def generate_text(self, messages: list[dict], system_prompt: str | None = None) -> str:
        raise NotImplementedError

