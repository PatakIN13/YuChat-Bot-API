package ru.rt.yuchatbotapi.api

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.model.*
import ru.rt.yuchatbotapi.exception.AuthenticationException
import ru.rt.yuchatbotapi.exception.RateLimitException
import ru.rt.yuchatbotapi.exception.YuChatApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HttpClientErrorTest {

    @Test
    fun `401 throws AuthenticationException`() = runBlocking {
        val client = createMockClient { _ ->
            respond("Unauthorized", HttpStatusCode.Unauthorized)
        }
        val api = MessagesApi(client)

        assertFailsWith<AuthenticationException> {
            api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")
        }
    }

    @Test
    fun `403 throws YuChatApiException with statusCode`() = runBlocking {
        val client = createMockClient { _ ->
            respond("Forbidden", HttpStatusCode.Forbidden)
        }
        val api = MessagesApi(client)

        val ex = assertFailsWith<YuChatApiException> {
            api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")
        }
        assertEquals(403, ex.statusCode)
    }

    @Test
    fun `429 throws RateLimitException after retries`() = runBlocking {
        var callCount = 0
        val client = createMockClient { _ ->
            callCount++
            respond("Too Many Requests", HttpStatusCode.TooManyRequests)
        }
        val api = MessagesApi(client)

        assertFailsWith<RateLimitException> {
            api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")
        }
        assertEquals(1, callCount)
    }

    @Test
    fun `500 throws YuChatApiException with error body`() = runBlocking {
        val client = createMockClient { _ ->
            respond(
                content = ByteReadChannel("""{"error":"Internal Server Error"}"""),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = MessagesApi(client)

        val ex = assertFailsWith<YuChatApiException> {
            api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")
        }
        assertEquals(500, ex.statusCode)
        assertTrue(ex.message!!.contains("Internal Server Error"))
    }

    @Test
    fun `bearer token is sent in request`() = runBlocking {
        var capturedAuth = ""
        val client = createMockClient { request ->
            capturedAuth = request.headers[HttpHeaders.Authorization] ?: ""
            jsonResponse("""{"messageId":"m1"}""")
        }
        val api = MessagesApi(client)

        api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")

        assertEquals("Bearer test-token", capturedAuth)
    }

    @Test
    fun `content type is application json`() = runBlocking {
        var capturedContentType = ""
        val client = createMockClient { request ->
            capturedContentType = request.body.contentType?.toString() ?: ""
            jsonResponse("""{"messageId":"m1"}""")
        }
        val api = MessagesApi(client)

        api.send(WorkspaceId("ws-1"), ChatId("chat-1"), "test")

        assertTrue(capturedContentType.contains("application/json"))
    }

    @Test
    fun `postNoContent handles 401`() = runBlocking {
        val client = createMockClient { _ ->
            respond("Unauthorized", HttpStatusCode.Unauthorized)
        }
        val api = ChatsApi(client)

        assertFailsWith<AuthenticationException> {
            api.leave(WorkspaceId("ws-1"), ChatId("chat-1"))
        }
    }

    @Test
    fun `postNoContent handles 429`() = runBlocking {
        val client = createMockClient { _ ->
            respond("Too Many Requests", HttpStatusCode.TooManyRequests)
        }
        val api = ChatsApi(client)

        assertFailsWith<RateLimitException> {
            api.archive(WorkspaceId("ws-1"), ChatId("chat-1"))
        }
    }
}
