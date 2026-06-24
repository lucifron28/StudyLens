import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")
django.setup()

from ai_services.service import get_provider

def test():
    try:
        provider = get_provider()
        print(f"Using provider: {provider.__class__.__name__}")
        print(f"Model: {getattr(provider, 'model', 'unknown')}")
        
        print("Sending test request to DeepSeek...")
        response = provider.generate_text([{"role": "user", "content": "Hello DeepSeek! What is 1+1? Please answer in one short sentence."}])
        print("\nResponse from AI:")
        print(response)
    except Exception as e:
        print(f"Error testing AI: {e}")

if __name__ == "__main__":
    test()
