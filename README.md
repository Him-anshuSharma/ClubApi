Hello! 😊 How can I assist you today?

# AI Key Manager for Android

A robust, pluggable library for managing API keys and making requests to multiple AI providers (Gemini, Hugging Face, OpenRouter, Groq, ArliAI, ShaleProtocol) with automatic key/provider rotation, model selection, retries, and error handling.

## Features
- 🔑 **API Key Management**: Securely store and rotate multiple keys per provider.
- 🤖 **Multi-Provider Support**: Gemini, Hugging Face, OpenRouter, Groq, ArliAI, ShaleProtocol.
- 🔄 **Round-Robin Rotation**: Evenly distribute requests across all keys and providers.
- 🧠 **Model Selection**: Specify models per provider or per request.
- 🔁 **Retries & Failover**: Retries on network/HTTP errors, automatic failover to next key/provider.
- 📊 **Metrics & Logging**: Logs timing, attempts, errors, and responses.
- 🛡️ **Robust JSON Parsing**: Uses Moshi for safe, reliable response parsing.
- ⏱️ **Global Timeout**: Set a global API call timeout (default 60 seconds, user-configurable).

## YAML Configuration Example
Create `key_storage.yaml` in your app's internal storage (or let the library create a template):

```yaml
api_keys:
  Gemini:
    keys:
      - YOUR_GEMINI_KEY1
      - YOUR_GEMINI_KEY2
    default_model: gemini-pro
  HuggingFace:
    keys:
      - YOUR_HF_KEY1
      - YOUR_HF_KEY2
    default_model: meta-llama/Llama-2-70b-chat-hf
  OpenRouter:
    keys:
      - YOUR_OPENROUTER_KEY1
    default_model: openrouter-model-1
  Groq:
    keys:
      - YOUR_GROQ_KEY1
    default_model: groq-model-1
  ArliAI:
    keys:
      - YOUR_ARLIAI_KEY1
    default_model: arliai-model-1
  ShaleProtocol:
    keys:
      - YOUR_SHALE_KEY1
    default_model: shale-model-1
```

## Installation
Add to your `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    // ...other dependencies
}
```

## Usage
```kotlin
import himanshu.com.apikeymanager.AiManager

// Default timeout (60 seconds):
val aiManager = AiManager(context)

// Custom timeout (e.g., 30 seconds):
val aiManager = AiManager(context, timeoutSeconds = 30)

// In a coroutine scope:
val result = aiManager.postRequest("Tell me a joke!")
println(result)
```
- The library will automatically try all keys and providers in round-robin order until one succeeds.
- You can set the global API call timeout (in seconds) when creating `AiManager`. All providers will use this timeout for their network calls.
- **The response from `postRequest` is always a plain string containing the assistant's reply.**

## How It Works
- **Key/Provider Rotation**: Each request uses the next (provider, key) pair. If a request fails (after retries), the next pair is tried.
- **Retries**: Each provider will retry up to 2 times on network/HTTP errors before failing over.
- **Metrics**: Logs timing, attempts, HTTP codes, and errors for each request.
- **Model Selection**: Pass a model name to `postRequest`, or set a default model on the provider.

## Supported Providers
- Gemini
- Hugging Face
- OpenRouter
- Groq
- ArliAI
- ShaleProtocol

## Error Handling
- If all keys/providers fail, an exception is thrown with the last error details.
- All errors, attempts, and responses are logged via `Log.d`/`Log.e`.

## Extending
- Add new providers by implementing the `AiProvider` interface.
- Customize retry logic, metrics, or key/model selection as needed.
- Use a more advanced YAML structure for per-provider models if desired.

## License
MIT or your preferred license.

## Response Format

- Returns only the assistant's reply as a string.
- Example:
  ```
  Hello, how can I help you today?
  ```

---

For all providers, the library attempts to extract the main assistant reply as a string, but you can access the raw response if you need more details. 