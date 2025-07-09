# API Key Manager Android Library

This Android library provides a unified interface to interact with multiple AI providers (Gemini, HuggingFace, OpenRouter, Groq, ArliAI, ShaleProtocol) in your own Android projects. It manages API keys and model selection via a JSON config file, and handles provider selection, key rotation, and error handling automatically.

## What does it do?
- Provides a unified interface to send prompts to multiple AI providers.
- Manages API keys and model selection for each provider via a JSON config file (`key_storage.json` in your assets).
- Handles provider selection, key rotation, and error handling automatically.
- Returns the AI response as a `String` for each provider call.

## Supported Providers
- **Gemini** (Google)
- **HuggingFace**
- **OpenRouter**
- **Groq**
- **ArliAI**
- **ShaleProtocol**

## Return Type
- The main API returns a `String` containing the AI-generated response for the given prompt.
- If an error occurs, an exception is thrown or an error message string is returned, depending on usage context.

## How to Add to Your Project (via JitPack)

### 1. Add JitPack to your repositories
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency
Replace `User`, `Repo`, and `Tag` with the correct values for this repository:
```kotlin
dependencies {
    implementation("com.github.User:Repo:Tag")
}
```

## How to Use

1. **Add your API keys**
   - Place a `key_storage.json` file in your app's `assets` directory:
   ```json
   {
     "api_keys": {
       "Gemini": {
         "keys": ["YOUR_GEMINI_API_KEY"],
         "default_model": "gemini-2.0-flash-lite"
       },
       "HuggingFace": {
         "keys": ["YOUR_HUGGINGFACE_API_KEY"],
         "default_model": "meta-llama/Llama-3.1-8B-Instruct"
       }
       // ... add other providers as needed
     }
   }
   ```

2. **Call the library from your code**
   ```kotlin
   val apiKeyManager = ApiKeyManager(context)
   val aiManager = AiManager(context)
   val response = runBlocking { aiManager.postRequest("Your prompt here") }
   println(response)
   ```
   - The library will automatically select a provider and key, send the prompt, and return the response as a String.

## Notes
- Providers without API keys in the JSON file are skipped and not used.
- Make sure you have a working internet connection.
- API keys should be kept secure and not shared publicly.

---

Feel free to contribute or open issues for improvements! 