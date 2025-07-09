package himanshu.com.apikeymanager

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import android.util.Log

class ApiKeyManager(private val context: Context) {
    private val fileName = "key_storage.yaml"
    private var apiKeys: Map<Any, List<String>> = mapOf()
    private var lastIndex = 0
    private var providerConfigs: Map<String, Map<String, Any>> = mapOf()

    init {
        loadKeys()
    }

    private fun loadKeys() {
        val mapper = ObjectMapper(YAMLFactory())
        try {
            context.assets.open(fileName).use { inputStream ->
                val data = mapper.readValue(inputStream, Map::class.java) as? Map<*, *>
                val apiKeysSection = data?.get("api_keys") as? Map<*, *>
                apiKeys = apiKeysSection?.entries
                    ?.mapNotNull { entry ->
                        val key = entry.key as? String ?: return@mapNotNull null
                        val values = when (val v = entry.value) {
                            is Map<*, *> -> (v["keys"] as? List<*>)?.filterIsInstance<String>() ?: listOf()
                            is List<*> -> v.filterIsInstance<String>()
                            is String -> listOf(v)
                            else -> listOf()
                        }
                        key to values
                    }?.toMap() ?: mapOf()
                // Store the full config for each provider (for default_model)
                providerConfigs = apiKeysSection?.entries
                    ?.mapNotNull { entry ->
                        val key = entry.key as? String ?: return@mapNotNull null
                        val value = entry.value as? Map<String, Any> ?: mapOf<String, Any>()
                        key to value.mapKeys { it.key.toString() }
                    }?.toMap() ?: mapOf()
            }
        } catch (e: Exception) {
            apiKeys = mapOf()
            providerConfigs = mapOf()
        }
    }

    fun getApiKeysForProvider(providerName: String): List<String> {
        loadKeys()
        return apiKeys[providerName] ?: listOf()
    }

    fun getApiKeyForProvider(providerName: String): String? {
        loadKeys()
        return apiKeys[providerName]?.firstOrNull()
    }

    @Synchronized
    fun getApiKey(): String {
        loadKeys()
        if (apiKeys.isEmpty()) throw Exception("No API keys found!")
        val key = apiKeys[(lastIndex % apiKeys.size)]?.firstOrNull()
        lastIndex = (lastIndex + 1) % apiKeys.size
        return key.toString()
    }

    // Optionally, expose all keys for custom rotation logic in the app
    fun getAllApiKeys(): List<String> {
        loadKeys()
        return apiKeys.flatMap { it -> it.value }
    }

    fun getDefaultModelForProvider(providerName: String): String? {
        loadKeys()
        val config = providerConfigs[providerName]
        return config?.get("default_model") as? String
    }
}

// Manual test entry point
fun main() {
    // Replace with your Android context if running in an Android environment
    val context: Context? = null // This must be set to a valid Context in a real app
    if (context == null) {
        println("No Android context available. Manual test cannot run in pure JVM.")
        return
    }
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
            val response = kotlinx.coroutines.runBlocking { provider.sendRequest(prompt) }
            println("${provider.name} response: $response")
        } catch (e: Exception) {
            println("${provider.name} failed: ${e.message}")
        }
    }
}

