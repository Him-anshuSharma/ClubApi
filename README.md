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

## YAML Configuration Example
Create `key_storage.yaml` in your app's internal storage (or let the library create a template):

```yaml
api_keys:
  Gemini:
    - YOUR_GEMINI_KEY1
    - YOUR_GEMINI_KEY2
  HuggingFace:
    - YOUR_HF_KEY1
  OpenRouter:
    - YOUR_OPENROUTER_KEY1
  Groq:
    - YOUR_GROQ_KEY1
  ArliAI:
    - YOUR_ARLIAI_KEY1
  ShaleProtocol:
    - YOUR_SHALE_KEY1
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

val aiManager = AiManager(context)
// In a coroutine scope:
val result = aiManager.postRequest("Tell me a joke!", model = "optional-model")
println(result)
```
- The library will automatically try all keys and providers in round-robin order until one succeeds.
- You can specify a model per request, or set a default model per provider in code.

## How It Works
- **Key/Provider Rotation**: Each request uses the next (provider, key) pair. If a request fails (after retries), the next pair is tried.
- **Retries**: Each provider will retry up to 2 times on network/HTTP errors before failing over.
- **Metrics**: Logs timing, attempts, HTTP codes, and errors for each request.
- **Model Selection**: Pass a model name to `postRequest`, or set a default model on the provider.

## Supported Providers
| Provider      | Endpoint Example                                         | Auth Header Example                      |
|--------------|----------------------------------------------------------|------------------------------------------|
| Gemini       | https://generativelanguage.googleapis.com/v1beta/...      | Authorization: Bearer YOUR_API_KEY       |
| Hugging Face | https://api-inference.huggingface.co/models/...          | Authorization: Bearer YOUR_HF_API_KEY    |
| OpenRouter   | https://openrouter.ai/api/v1/chat/completions            | Authorization: Bearer YOUR_API_KEY       |
| Groq         | https://api.groq.com/openai/v1/chat/completions           | Authorization: Bearer YOUR_API_KEY       |
| ArliAI       | https://api.arliai.com/v1/chat/completions                | Authorization: Bearer YOUR_ARLIAI_API_KEY|
| ShaleProtocol| https://shale.live/v1/chat/completions                    | Authorization: Bearer YOUR_API_KEY       |

## Error Handling
- If all keys/providers fail, an exception is thrown with the last error details.
- All errors, attempts, and responses are logged via `Log.d`/`Log.e`.

## Extending
- Add new providers by implementing the `AiProvider` interface.
- Customize retry logic, metrics, or key/model selection as needed.
- Use a more advanced YAML structure for per-provider models if desired.

## License
MIT or your preferred license.

## Test App

A simple test app is included. To run it:

1. Build and run the app on an emulator or device.
2. On first launch, the app will create a `key_storage.yaml` file in your app's internal storage directory.
3. Use Android Studio's Device File Explorer to navigate to `/data/data/himanshu.com.apikeymanager/files/key_storage.yaml` and add your API keys under `api_keys:` as shown above.
4. Relaunch the app to see the API key displayed. Tap the button to cycle through keys.

## Publishing

### JitPack (Recommended for GitHub Projects)
1. Push your project to a public GitHub repository.
2. Go to [jitpack.io](https://jitpack.io/), search for your repo, and follow the instructions.
3. Add the JitPack repository and dependency to your app's `build.gradle`:
   ```gradle
   repositories {
       maven { url 'https://jitpack.io' }
   }
   dependencies {
       implementation 'com.github.yourusername:yourrepo:Tag'
   }
   ```

### Maven Central
- Follow [Maven Central's publishing guide](https://central.sonatype.org/publish/publish-guide/).

## Response Format

### HuggingFaceProvider
- Returns only the assistant's reply as a string, but the raw response may include extra tags (e.g., <think>...</think>) and reasoning if the model provides it.
- Example:
  ```
  <think>
  ...model reasoning...
  </think>
  Hello! 👋 How can I assist you today? 😊
  ```
- The library extracts the main reply, but you may see extra context if the model includes it.

### OpenRouterProvider
- Returns the full JSON response from the OpenRouter API, which includes metadata and the assistant's reply.
- Example:
  ```json
  {
    "id": "gen-1751891938-Smap2Jtdrbwntrq5WbKn",
    "provider": "Chutes",
    "model": "mistralai/mistral-small-3.2-24b-instruct:free",
    "object": "chat.completion",
    "created": 1751891938,
    "choices": [
      {
        "logprobs": null,
        "finish_reason": "stop",
        "native_finish_reason": "stop",
        "index": 0,
        "message": {
          "role": "assistant",
          "content": "Hello! 😊 How can I assist you today?",
          "refusal": null,
          "reasoning": null
        }
      }
    ],
    "usage": {
      "prompt_tokens": 9,
      "completion_tokens": 13,
      "total_tokens": 22
    }
  }
  ```
- The library extracts the `choices[0].message.content` field as the main reply.

### GroqProvider
- Returns only the assistant's reply as a string.
- Example:
  ```
  Hello, how can I help you today?
  ```

---

For all providers, the library attempts to extract the main assistant reply as a string, but you can access the raw response if you need more details. 