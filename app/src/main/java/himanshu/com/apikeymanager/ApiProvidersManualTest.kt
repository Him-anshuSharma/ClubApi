package himanshu.com.apikeymanager

import android.content.Context
import kotlinx.coroutines.runBlocking

fun runApiProvidersManualTest(context: Context) {
    val apiKeyManager = ApiKeyManager(context)
    val providers = listOf(
        GeminiProvider(apiKeyManager),
        HuggingFaceProvider(apiKeyManager),
        OpenRouterProvider(apiKeyManager),
        GroqProvider(apiKeyManager)
        // Add ArliAIProvider and ShaleProtocolProvider if needed
    )
    val prompt = "Say hello from the test!"
    providers.forEach { provider ->
        try {
            provider.setApiKey(apiKeyManager.getApiKeyForProvider(provider.name) ?: "")
            val response = runBlocking { provider.sendRequest(prompt) }
            println("${provider.name} response: $response")
        } catch (e: Exception) {
            println("${provider.name} failed: ${e.message}")
        }
    }
} 