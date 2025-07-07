package himanshu.com.apikeymanager

import android.content.Context
import org.yaml.snakeyaml.Yaml
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
        val yaml = Yaml()
        try {
            context.assets.open(fileName).use { inputStream ->
                val data = yaml.load(inputStream.reader()) as? Map<*, *>
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

