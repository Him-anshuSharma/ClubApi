

# API Key Manager Android Library

This Android library provides a unified interface to interact with multiple AI providers (Gemini, HuggingFace, OpenRouter, Groq, ArliAI, ShaleProtocol) in your own Android projects. It manages API keys and model selection via a JSON config file, and handles provider selection, key rotation, and error handling automatically.

### [JitPack Link (Latest Release)](https://jitpack.io/#Him-anshuSharma/ClubApi)

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
    implementation("com.github.Him-anshuSharma:ClubApi:v5.0.2")
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

## ProGuard/R8 Rules
If you use ProGuard or R8, add the following rules to your `proguard-rules.pro` to ensure proper operation and JSON parsing:

```proguard
# --- Keep all classes and members in your API key manager library ---
-keep class himanshu.com.apikeymanager.** { *; }
-keepclassmembers class himanshu.com.apikeymanager.** { *; }

# --- Keep data classes for JSON parsing (Moshi/Gson) ---
-keep class himanshu.com.apikeymanager.ProviderConfig { *; }
-keep class himanshu.com.apikeymanager.KeyStorage { *; }


# --- Moshi (for Kotlin reflection, codegen, adapters) ---
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }
-dontwarn com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.yaml.snakeyaml.Yaml

# --- Gson (if used) ---
-keep class com.google.gson.** { *; }

# --- Keep annotation attributes and signatures (important for Moshi/Gson) ---
-keepattributes *Annotation*
-keepattributes Signature
```

## Notes
- Providers without API keys in the JSON file are skipped and not used.
- Make sure you have a working internet connection.
- API keys should be kept secure and not shared publicly.

---

Feel free to contribute or open issues for improvements!