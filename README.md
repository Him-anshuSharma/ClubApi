Hello! 😊 How can I assist you today?

# API Key Manager (v3.x)

## Overview
This library manages API keys and model selection for multiple AI providers in your Android app. It now uses Moshi for JSON serialization and stores keys in `key_storage.json`.

## Setup

1. **Add the dependency**
   (If using JitPack or Maven, update as needed)
   ```kotlin
   implementation("com.github.Him-anshuSharma:apikeymanager:3.0.2")
   implementation("com.squareup.moshi:moshi:1.15.0")
   implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
   // No codegen or annotation processor needed!
   ```

2. **Add your API keys**
   - Place a `key_storage.json` file in your app's `assets` directory:
     ```json
     {
       "api_keys": {
         "Gemini": {
           "keys": ["your-gemini-key1"],
           "default_model": "gemini-2.0-flash-lite"
         },
         "HuggingFace": {
           "keys": ["your-huggingface-key1"],
           "default_model": "meta-llama/Llama-3.1-8B-Instruct"
         },
         "OpenRouter": {
           "keys": ["your-openrouter-key1"],
           "default_model": "google/gemma-3-27b-it:free"
         },
         "Groq": {
           "keys": ["your-groq-key1"],
           "default_model": "llama-3.3-70b-versatile"
         }
       }
     }
     ```
   - **Do not commit your real `key_storage.json` to version control!** Add it to your `.gitignore`.

3. **Usage Example**
   ```kotlin
   val apiKeyManager = ApiKeyManager(context)
   val aiManager = AiManager(context)
   val response = runBlocking { aiManager.postRequest("Your prompt here") }
   println(response)
   ```

4. **Remove old YAML files (for 3.0.2 and older versions)**
   - Delete any `key_storage.yaml` or similar files from your project.

## Notes
- All serialization is handled by Moshi **reflection** (no codegen, no annotation processor required).
- Only `key_storage.json` is supported for key storage.
- Make sure to keep your API keys secure and out of version control. 