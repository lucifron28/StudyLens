from openai import OpenAI
from ai_services.providers.base import AIProviderError, BaseAIProvider

class DeepSeekProvider(BaseAIProvider):
    """
    Implementation of DeepSeek AI using the openai python package.
    DeepSeek's API is compatible with OpenAI's API.
    """

    def __init__(self, api_key: str, model: str):
        if not api_key:
            raise AIProviderError("DeepSeek is not configured. Set DEEPSEEK_API_KEY on the backend.")
        self.api_key = api_key
        # Default to deepseek-v4-flash if no model is provided
        self.model = model if model else "deepseek-v4-flash"
        
        # DeepSeek base URL
        self.client = OpenAI(api_key=self.api_key, base_url="https://api.deepseek.com")

    def generate_text(self, messages: list[dict], system_prompt: str | None = None) -> str:
        # Construct the messages list
        api_messages = []
        if system_prompt:
            api_messages.append({"role": "system", "content": system_prompt})
        
        # The frontend/backend messages format usually contains "role" and "content"
        # We just need to map it if needed, but it's already compatible for user/assistant roles.
        for msg in messages:
            role = msg.get("role", "user")
            # OpenAI API roles: system, user, assistant
            if role not in ("system", "user", "assistant"):
                role = "user"
            api_messages.append({
                "role": role,
                "content": msg.get("content", "")
            })

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=api_messages,
                stream=False
            )
            return response.choices[0].message.content
        except Exception as e:
            raise AIProviderError(f"DeepSeek generation failed: {str(e)}")
