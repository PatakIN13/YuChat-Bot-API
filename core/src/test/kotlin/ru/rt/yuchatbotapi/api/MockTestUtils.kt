package ru.rt.yuchatbotapi.api

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import ru.rt.yuchatbotapi.internal.YuChatHttpClient

/**
 * Создаёт YuChatHttpClient с MockEngine для тестов.
 */
internal fun createMockClient(
    handler: MockRequestHandler
): YuChatHttpClient {
    val mockEngine = MockEngine(handler)
    return YuChatHttpClient(
        token = "test-token",
        baseUrl = "https://test.yuchat.ai",
        maxRetries = 1,
        retryDelayMs = 10L,
        engine = mockEngine
    )
}

/** Стандартный JSON-ответ 200 OK. */
internal fun MockRequestHandleScope.jsonResponse(json: String) = respond(
    content = ByteReadChannel(json),
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json")
)

/** Пустой 200 OK. */
internal fun MockRequestHandleScope.emptyOkResponse() = respond(
    content = ByteReadChannel(""),
    status = HttpStatusCode.OK
)

/** Извлечение текста тела запроса из HttpRequestData. */
internal fun HttpRequestData.bodyAsText(): String {
    val content = body
    return when (content) {
        is TextContent -> content.text
        is OutgoingContent.ByteArrayContent -> String(content.bytes())
        else -> ""
    }
}
