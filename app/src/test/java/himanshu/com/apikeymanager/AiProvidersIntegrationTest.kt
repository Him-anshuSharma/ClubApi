package himanshu.com.apikeymanager

import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.yaml.snakeyaml.Yaml
import java.io.File

class AiProvidersIntegrationTest {
    data class ProviderConfig(val keys: List<String>?, val default_model: String?)
    data class ApiKeys(val api_keys: Map<String, ProviderConfig>)

    private fun loadKeys(): Map<String, ProviderConfig> {
        val yaml = Yaml()
        val file = File("app/src/main/assets/key_storage.yaml")
        val data = yaml.load<Map<String, Any>>(file.inputStream())
        val apiKeys = data["api_keys"] as Map<String, Map<String, Any>>
        return apiKeys.mapValues { (_, v) ->
            val keys = (v["keys"] as? List<*>)?.filterIsInstance<String>()
            val model = v["default_model"] as? String
            ProviderConfig(keys, model)
        }
    }

    @Test
    fun testGeminiProvider_realApi() = runBlocking {
        val keys = loadKeys()
        val providerConfig = keys["Gemini"]
        val apiKey = providerConfig?.keys?.firstOrNull() ?: error("No Gemini API key found")
        val provider = GeminiProvider()
        provider.setApiKey(apiKey)
        val prompt = "Say hello from Gemini!"
        val response = provider.sendRequest(prompt)
        println("Gemini API response: $response")
        assertTrue(response.isNotBlank())
    }

    @Test
    fun testHuggingFaceProvider_realApi() = runBlocking {
        val keys = loadKeys()
        val providerConfig = keys["HuggingFace"]
        val apiKey = providerConfig?.keys?.firstOrNull() ?: error("No HuggingFace API key found")
        val provider = HuggingFaceProvider()
        provider.setApiKey(apiKey)
        val prompt = "Say hello from HuggingFace!"
        val response = provider.sendRequest(prompt)
        println("HuggingFace API response: $response")
        assertTrue(response.isNotBlank())
    }

    @Test
    fun testGroqProvider_realApi() = runBlocking {
        val keys = loadKeys()
        val providerConfig = keys["Groq"]
        val apiKey = providerConfig?.keys?.firstOrNull() ?: error("No Groq API key found")
        val provider = GroqProvider()
        provider.setApiKey(apiKey)
        val prompt = "Say hello from Groq!"
        val response = provider.sendRequest(prompt)
        println("Groq API response: $response")
        assertTrue(response.isNotBlank())
    }

    @Test
    fun testOpenRouterProvider_realApi() = runBlocking {
        val keys = loadKeys()
        val providerConfig = keys["OpenRouter"]
        val apiKey = providerConfig?.keys?.firstOrNull() ?: error("No OpenRouter API key found")
        val provider = OpenRouterProvider()
        provider.setApiKey(apiKey)
        val prompt = "Say hello from OpenRouter!"
        val response = provider.sendRequest(prompt)
        println("OpenRouter API response: $response")
        assertTrue(response.isNotBlank())
    }
} 