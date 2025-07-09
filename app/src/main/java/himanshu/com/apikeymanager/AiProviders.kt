package himanshu.com.apikeymanager

import android.util.Log
import okhttp3.*
import com.squareup.moshi.*
import kotlin.system.measureTimeMillis
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass
import java.util.concurrent.TimeUnit

interface AiProvider {
    val name: String
    var apiKeyManager: ApiKeyManager?
    fun setApiKey(key: String)
    suspend fun sendRequest(prompt: String): String
}

// Data classes for requests and responses
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

// Response models
@JsonClass(generateAdapter = true)
private data class GeminiCandidate(val content: GeminiContent?)
@JsonClass(generateAdapter = true)
private data class GeminiContent(val parts: List<GeminiPart>?)
@JsonClass(generateAdapter = true)
private data class GeminiPart(val text: String?)
@JsonClass(generateAdapter = true)
private data class GeminiResponse(val candidates: List<GeminiCandidate>?)
@JsonClass(generateAdapter = true)
private data class ChatChoice(val message: ChatMessage?)
@JsonClass(generateAdapter = true)
private data class ChatMessage(val content: String?)
@JsonClass(generateAdapter = true)
private data class ChatResponse(val choices: List<ChatChoice>?)

private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

class GeminiProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "Gemini"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "gemini-2.5-pro"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$useModel:generateContent"
        val requestObj = GeminiRequest(listOf(GeminiContentRequest(listOf(GeminiPartRequest(prompt)))))
        val jsonBody = moshi.adapter(GeminiRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("Gemini API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(GeminiResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return text ?: throw Exception("Failed to parse Gemini response: $body")
    }
}

class HuggingFaceProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "HuggingFace"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "Qwen/Qwen2-7B-Instruct"
        val url = "https://router.huggingface.co/featherless-ai/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("HuggingFace API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(ChatResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.choices?.firstOrNull()?.message?.content
        return text ?: throw Exception("Failed to parse HuggingFace response: $body")
    }
}

class OpenRouterProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "OpenRouter"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "openrouter-model"
        val url = "https://openrouter.ai/api/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("OpenRouter API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(ChatResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.choices?.firstOrNull()?.message?.content
        return text ?: throw Exception("Failed to parse OpenRouter response: $body")
    }
}

class GroqProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "Groq"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "llama2-70b-4096"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("Groq API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(ChatResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.choices?.firstOrNull()?.message?.content
        return text ?: throw Exception("Failed to parse Groq response: $body")
    }
}

class ArliAIProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "ArliAI"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "arliai-model"
        val url = "https://api.arliai.com/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("ArliAI API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(ChatResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.choices?.firstOrNull()?.message?.content
        return text ?: throw Exception("Failed to parse ArliAI response: $body")
    }
}

class ShaleProtocolProvider(
    override var apiKeyManager: ApiKeyManager? = null,
    private val timeoutSeconds: Long = 60,
    var client: OkHttpClient = OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
) : AiProvider {
    override val name = "ShaleProtocol"
    private var apiKey: String = ""
    override fun setApiKey(key: String) { apiKey = key }
    override suspend fun sendRequest(prompt: String): String {
        val useModel = apiKeyManager?.getDefaultModelForProvider(name) ?: "shale-model"
        val url = "https://shale.live/v1/chat/completions"
        val requestObj = OpenAIChatRequest(useModel, listOf(OpenAIMessage("user", prompt)))
        val jsonBody = moshi.adapter(OpenAIChatRequest::class.java).toJson(requestObj)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful) throw Exception("ShaleProtocol API HTTP error: ${response.code} - $body")
        val adapter = moshi.adapter(ChatResponse::class.java)
        val parsed = adapter.fromJson(body ?: "")
        val text = parsed?.choices?.firstOrNull()?.message?.content
        return text ?: throw Exception("Failed to parse ShaleProtocol response: $body")
    }
} 