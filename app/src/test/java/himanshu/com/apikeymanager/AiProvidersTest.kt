package himanshu.com.apikeymanager

import org.junit.Test
import org.junit.Assert.*
import okhttp3.*
import kotlinx.coroutines.runBlocking
import android.content.Context
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import io.mockk.every
import io.mockk.mockkStatic
import android.util.Log
import org.junit.BeforeClass

class AiProvidersTest {
    private val mockContext: Context = mock()
    private val mockApiKeyManager: ApiKeyManager = mock()

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupLog() {
            mockkStatic(Log::class)
            every { Log.d(any<String>(), any<String>()) } returns 0
            every { Log.e(any<String>(), any<String>()) } returns 0
            every { Log.i(any<String>(), any<String>()) } returns 0
            every { Log.w(any<String>(), any<String>()) } returns 0
            every { Log.v(any<String>(), any<String>()) } returns 0
        }
    }

    @Test
    fun testGeminiProvider_sendRequest_success() = runBlocking {
        val expectedText = "Gemini response text"
        val fakeBody = """{"candidates":[{"content":{"parts":[{"text":"$expectedText"}]}}]}"""
        val provider = GeminiProvider(mockApiKeyManager)
        provider.setApiKey("FAKE_KEY")
        // Mock ApiKeyManager
        whenever(mockApiKeyManager.getDefaultModelForProvider("Gemini")).thenReturn("gemini-2.5-pro")
        // Mock OkHttpClient
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()
        whenever(mockBody.string()).thenReturn(fakeBody)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body).thenReturn(mockBody)
        val mockCall = mock<Call>()
        whenever(mockCall.execute()).thenReturn(mockResponse)
        val mockClient = mock<OkHttpClient>()
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        // Inject mock client
        val clientField = GeminiProvider::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(provider, mockClient)
        val result = provider.sendRequest("hello?")
        assertEquals(expectedText, result)
    }

    @Test
    fun testHuggingFaceProvider_sendRequest_success() = runBlocking {
        val expectedText = "HF response text"
        val fakeBody = """{"choices":[{"message":{"content":"$expectedText"}}]}"""
        val provider = HuggingFaceProvider(mockApiKeyManager)
        provider.setApiKey("FAKE_KEY")
        whenever(mockApiKeyManager.getDefaultModelForProvider("HuggingFace")).thenReturn("deepseek-ai/DeepSeek-R1-0528")
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()
        whenever(mockBody.string()).thenReturn(fakeBody)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body).thenReturn(mockBody)
        val mockCall = mock<Call>()
        whenever(mockCall.execute()).thenReturn(mockResponse)
        val mockClient = mock<OkHttpClient>()
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        val clientField = HuggingFaceProvider::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(provider, mockClient)
        val result = provider.sendRequest("hello?")
        assertEquals(expectedText, result)
    }

    @Test
    fun testGroqProvider_sendRequest_success() = runBlocking {
        val expectedText = "Groq response text"
        val fakeBody = """{"choices":[{"message":{"content":"$expectedText"}}]}"""
        val provider = GroqProvider(mockApiKeyManager)
        provider.setApiKey("FAKE_KEY")
        whenever(mockApiKeyManager.getDefaultModelForProvider("Groq")).thenReturn("llama-3.3-70b-versatile")
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()
        whenever(mockBody.string()).thenReturn(fakeBody)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body).thenReturn(mockBody)
        val mockCall = mock<Call>()
        whenever(mockCall.execute()).thenReturn(mockResponse)
        val mockClient = mock<OkHttpClient>()
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        val clientField = GroqProvider::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(provider, mockClient)
        val result = provider.sendRequest("hello?")
        assertEquals(expectedText, result)
    }

    @Test
    fun testOpenRouterProvider_sendRequest_success() = runBlocking {
        val expectedText = "Router response text"
        val fakeBody = """{"choices":[{"message":{"content":"$expectedText"}}]}"""
        val provider = OpenRouterProvider(mockApiKeyManager)
        provider.setApiKey("FAKE_KEY")
        whenever(mockApiKeyManager.getDefaultModelForProvider("OpenRouter")).thenReturn("openrouter-model")
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()
        whenever(mockBody.string()).thenReturn(fakeBody)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body).thenReturn(mockBody)
        val mockCall = mock<Call>()
        whenever(mockCall.execute()).thenReturn(mockResponse)
        val mockClient = mock<OkHttpClient>()
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        val clientField = OpenRouterProvider::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(provider, mockClient)
        val result = provider.sendRequest("hello?")
        assertEquals(expectedText, result)
    }

    // Add provider-specific tests here
} 