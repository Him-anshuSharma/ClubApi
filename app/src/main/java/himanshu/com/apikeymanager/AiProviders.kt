package himanshu.com.apikeymanager

import android.util.Log
import okhttp3.*
import com.squareup.moshi.*
import kotlin.system.measureTimeMillis
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass

interface AiProvider {
    val name: String
    var apiKeyManager: ApiKeyManager?
    fun setApiKey(key: String)
    suspend fun sendRequest(prompt: String): String
}

// Gemini response data class
private data class GeminiCandidate(val content: GeminiContent?)
private data class GeminiContent(val parts: List<GeminiPart>?)
private data class GeminiPart(val text: String?)
private data class GeminiResponse(val candidates: List<GeminiCandidate>?)

// HuggingFace response data class
private data class HuggingFaceResponse(val generated_text: String?)

// OpenRouter, Groq, ArliAI, ShaleProtocol response data class
private data class ChatChoice(val message: ChatMessage?)
private data class ChatMessage(val content: String?)
private data class ChatResponse(val choices: List<ChatChoice>?)

@JsonClass(generateAdapter = true)
data class OpenAIMessage(val role: String, val content: String)
@JsonClass(generateAdapter = true)
data class OpenAIChatRequest(val model: String, val messages: List<OpenAIMessage>)
@JsonClass(generateAdapter = true)
data class GeminiPartRequest(val text: String)
@JsonClass(generateAdapter = true)
data class GeminiContentRequest(val parts: List<GeminiPartRequest>)
@JsonClass(generateAdapter = true)
data class GeminiRequest(val contents: List<GeminiContentRequest>)

private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

class GeminiProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "Gemini"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "gemini-2.5-pro"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$useModel:generateContent"
        val requestObj = GeminiRequest(listOf(GeminiContentRequest(listOf(GeminiPartRequest(prompt)))))
        val jsonBody = moshi.adapter(GeminiRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("GeminiProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("GeminiProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("Gemini API HTTP error: ${response.code} - $body")
                    }
                    Log.d("GeminiProvider", "Response: $body")
                    val adapter = moshi.adapter(GeminiResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text == null) {
                        Log.e("GeminiProvider", "Failed to parse Gemini response: $body")
                        throw Exception("Failed to parse Gemini response: $body")
                    }
                    Log.d("GeminiProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("GeminiProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("GeminiProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("GeminiProvider: Unknown error")
    }
}

class HuggingFaceProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "HuggingFace"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "deepseek-ai/DeepSeek-R1-0528"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://router.huggingface.co/nebius/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("HuggingFaceProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("HuggingFaceProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("HuggingFace API HTTP error: ${response.code} - $body")
                    }
                    Log.d("HuggingFaceProvider", "Response: $body")
                    // Parse OpenAI-style chat completion response
                    val adapter = moshi.adapter(ChatResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.choices?.firstOrNull()?.message?.content
                    if (text == null) {
                        Log.e("HuggingFaceProvider", "Failed to parse HuggingFace response: $body")
                        throw Exception("Failed to parse HuggingFace response: $body")
                    }
                    Log.d("HuggingFaceProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("HuggingFaceProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("HuggingFaceProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("HuggingFaceProvider: Unknown error")
    }
}

class OpenRouterProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "OpenRouter"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "openrouter-model"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://openrouter.ai/api/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("OpenRouterProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("OpenRouterProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("OpenRouter API HTTP error: ${response.code} - $body")
                    }
                    Log.d("OpenRouterProvider", "Response: $body")
                    val adapter = moshi.adapter(ChatResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.choices?.firstOrNull()?.message?.content
                    if (text == null) {
                        Log.e("OpenRouterProvider", "Failed to parse OpenRouter response: $body")
                        throw Exception("Failed to parse OpenRouter response: $body")
                    }
                    Log.d("OpenRouterProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("OpenRouterProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("OpenRouterProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("OpenRouterProvider: Unknown error")
    }
}

class GroqProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "Groq"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "llama2-70b-4096"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("GroqProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("GroqProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("Groq API HTTP error: ${response.code} - $body")
                    }
                    Log.d("GroqProvider", "Response: $body")
                    val adapter = moshi.adapter(ChatResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.choices?.firstOrNull()?.message?.content
                    if (text == null) {
                        Log.e("GroqProvider", "Failed to parse Groq response: $body")
                        throw Exception("Failed to parse Groq response: $body")
                    }
                    Log.d("GroqProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("GroqProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("GroqProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("GroqProvider: Unknown error")
    }
}

class ArliAIProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "ArliAI"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "arliai-model"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://api.arliai.com/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("ArliAIProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("ArliAIProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("ArliAI API HTTP error: ${response.code} - $body")
                    }
                    Log.d("ArliAIProvider", "Response: $body")
                    val adapter = moshi.adapter(ChatResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.choices?.firstOrNull()?.message?.content
                    if (text == null) {
                        Log.e("ArliAIProvider", "Failed to parse ArliAI response: $body")
                        throw Exception("Failed to parse ArliAI response: $body")
                    }
                    Log.d("ArliAIProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("ArliAIProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("ArliAIProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("ArliAIProvider: Unknown error")
    }
}

class ShaleProtocolProvider(override var apiKeyManager: ApiKeyManager? = null) : AiProvider {
    override val name = "ShaleProtocol"
    private var apiKey: String = ""
    private val client = OkHttpClient()
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "shale-model"
        Log.d("${name}Provider", "Using model: $useModel")
        val url = "https://shale.live/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        var lastException: Exception? = null
        var lastBody: String? = null
        var lastCode: Int? = null
        var attempt = 0
        val maxRetries = 2
        val time = measureTimeMillis {
            while (attempt <= maxRetries) {
                try {
                    Log.d("ShaleProtocolProvider", "Sending request to $url with model=$useModel (attempt $attempt)")
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    lastBody = body
                    lastCode = response.code
                    if (!response.isSuccessful) {
                        Log.e("ShaleProtocolProvider", "HTTP error: ${response.code} - $body")
                        throw Exception("ShaleProtocol API HTTP error: ${response.code} - $body")
                    }
                    Log.d("ShaleProtocolProvider", "Response: $body")
                    val adapter = moshi.adapter(ChatResponse::class.java)
                    val parsed = adapter.fromJson(body ?: "")
                    val text = parsed?.choices?.firstOrNull()?.message?.content
                    if (text == null) {
                        Log.e("ShaleProtocolProvider", "Failed to parse ShaleProtocol response: $body")
                        throw Exception("Failed to parse ShaleProtocol response: $body")
                    }
                    Log.d("ShaleProtocolProvider", "Parsed text: $text")
                    return text
                } catch (e: Exception) {
                    Log.e("ShaleProtocolProvider", "Request failed (attempt $attempt): ${e.message}", e)
                    lastException = e
                    attempt++
                }
            }
        }
        Log.d("ShaleProtocolProvider", "Total time: ${time}ms, attempts: $attempt, lastCode: $lastCode, lastBody: $lastBody")
        throw lastException ?: Exception("ShaleProtocolProvider: Unknown error")
    }
} 