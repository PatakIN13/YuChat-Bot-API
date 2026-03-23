package ru.rt.yuchatbotapi.internal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.exception.AuthenticationException
import ru.rt.yuchatbotapi.exception.RateLimitException
import ru.rt.yuchatbotapi.exception.YuChatApiException

internal class YuChatHttpClient(
    private val token: String,
    private val baseUrl: String,
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000L,
    private val connectTimeoutMs: Long = 10_000L,
    private val requestTimeoutMs: Long = 60_000L,
    private val socketTimeoutMs: Long = 60_000L,
    engine: HttpClientEngine? = null
) {
    private val logger = LoggerFactory.getLogger(YuChatHttpClient::class.java)

    internal val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    internal val httpClient = HttpClient(engine ?: CIO.create()) {
        install(ContentNegotiation) {
            json(this@YuChatHttpClient.json)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = this@YuChatHttpClient.connectTimeoutMs
            requestTimeoutMillis = this@YuChatHttpClient.requestTimeoutMs
            socketTimeoutMillis = this@YuChatHttpClient.socketTimeoutMs
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer ${this@YuChatHttpClient.token}")
            contentType(ContentType.Application.Json)
        }
    }

    suspend inline fun <reified T> post(path: String, body: Any? = null, requestTimeoutOverrideMs: Long? = null): T {
        return executeWithRetry {
            val response = httpClient.post("${baseUrl}${path}") {
                if (body != null) setBody(body)
                if (requestTimeoutOverrideMs != null) {
                    timeout { requestTimeoutMillis = requestTimeoutOverrideMs }
                }
            }
            handleResponse(response)
        }
    }

    suspend inline fun <reified T> get(path: String): T {
        return executeWithRetry {
            val response = httpClient.get("${baseUrl}${path}")
            handleResponse(response)
        }
    }

    suspend inline fun <reified T> get(path: String, queryParams: Map<String, Any?>, requestTimeoutOverrideMs: Long? = null): T {
        return executeWithRetry {
            val response = httpClient.get("${baseUrl}${path}") {
                queryParams.forEach { (key, value) ->
                    if (value != null) parameter(key, value)
                }
                if (requestTimeoutOverrideMs != null) {
                    timeout { requestTimeoutMillis = requestTimeoutOverrideMs }
                }
            }
            handleResponse(response)
        }
    }

    suspend inline fun <reified T> delete(path: String): T {
        return executeWithRetry {
            val response = httpClient.delete("${baseUrl}${path}")
            handleResponse(response)
        }
    }

    suspend fun deleteNoContent(path: String) {
        executeWithRetry {
            val response = httpClient.delete("${baseUrl}${path}")
            handleResponseNoContent(response)
        }
    }

    suspend fun postNoContent(path: String, body: Any? = null) {
        executeWithRetry {
            val response = httpClient.post("${baseUrl}${path}") {
                if (body != null) setBody(body)
            }
            handleResponseNoContent(response)
        }
    }

    suspend inline fun <reified T> handleResponse(response: HttpResponse): T {
        when (response.status.value) {
            in 200..299 -> return response.body<T>()
            401 -> throw AuthenticationException()
            403 -> throw YuChatApiException("Forbidden", 403)
            429 -> throw RateLimitException()
            else -> {
                val errorBody = runCatching { response.bodyAsText() }.getOrDefault("")
                throw YuChatApiException(
                    "API error ${response.status.value}: $errorBody",
                    response.status.value
                )
            }
        }
    }

    suspend fun handleResponseNoContent(response: HttpResponse) {
        when (response.status.value) {
            in 200..299 -> return
            401 -> throw AuthenticationException()
            403 -> throw YuChatApiException("Forbidden", 403)
            429 -> throw RateLimitException()
            else -> {
                val errorBody = runCatching { response.bodyAsText() }.getOrDefault("")
                throw YuChatApiException(
                    "API error ${response.status.value}: $errorBody",
                    response.status.value
                )
            }
        }
    }

    suspend inline fun <T> executeWithRetry(block: () -> T): T {
        var lastException: Throwable? = null
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: RateLimitException) {
                lastException = e
                val delayMs = retryDelayMs * (1L shl attempt)
                logger.warn("Rate limited, retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                delay(delayMs)
            }
        }
        throw lastException ?: YuChatApiException("Max retries exceeded")
    }

    fun close() {
        httpClient.close()
    }
}
