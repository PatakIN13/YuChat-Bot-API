package ru.rt.yuchatbotapi.api

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessagesApiMockTest {

    @Test
    fun `send message v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"messageId":"msg-123"}""")
        }
        val api = MessagesApi(client)

        val result = api.send("ws-1", "chat-1", "Hello")

        assertEquals("/public/v1/chat.message.send", capturedPath)
        assertEquals("msg-123", result.messageId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"chatId\":\"chat-1\""))
        assertTrue(capturedBody.contains("\"markdown\":\"Hello\""))
    }

    @Test
    fun `send message v1 with fileIds and replyTo`() = runBlocking {
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedBody = request.bodyAsText()
            jsonResponse("""{"messageId":"msg-456"}""")
        }
        val api = MessagesApi(client)

        api.send("ws-1", "chat-1", "With files", listOf("file-1", "file-2"), "reply-msg")

        assertTrue(capturedBody.contains("\"fileIds\":[\"file-1\",\"file-2\"]"))
        assertTrue(capturedBody.contains("\"replyTo\":\"reply-msg\""))
    }

    @Test
    fun `edit message v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"updatedAt":"2024-01-01T00:00:00Z"}""")
        }
        val api = MessagesApi(client)

        val result = api.edit("ws-1", "chat-1", "msg-1", "Updated")

        assertEquals("/public/v1/chat.message.edit", capturedPath)
        assertEquals("2024-01-01T00:00:00Z", result.updatedAt)
    }

    @Test
    fun `delete message v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"updatedAt":"2024-01-01T00:00:00Z"}""")
        }
        val api = MessagesApi(client)

        api.delete("ws-1", "chat-1", "msg-1")

        assertEquals("/public/v1/chat.message.delete", capturedPath)
    }

    @Test
    fun `forward message v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"messageId":"fwd-1"}""")
        }
        val api = MessagesApi(client)

        val result = api.forward("ws-1", "chat-src", "msg-1", "chat-dst", "Forwarded")

        assertEquals("/public/v1/chat.message.forward", capturedPath)
        assertEquals("fwd-1", result.messageId)
    }

    @Test
    fun `sendV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"messageId":"msg-v2"}""")
        }
        val api = MessagesApi(client)

        val result = api.sendV2(workspaceId = "ws-1", chatId = "chat-1", text = "Hello v2")

        assertEquals("/public/v2/sendMessage", capturedPath)
        assertEquals("msg-v2", result.messageId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"chatId\":\"chat-1\""))
    }

    @Test
    fun `pin calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = MessagesApi(client)

        api.pin("ws-1", "chat-1", "msg-1")

        assertEquals("/public/v2/pinMessage", capturedPath)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `unpin calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = MessagesApi(client)

        api.unpin("ws-1", "chat-1", "msg-1")

        assertEquals("/public/v2/unpinMessage", capturedPath)
    }

    @Test
    fun `toggleReaction calls correct endpoint and returns result`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"reactionAdded":true}""")
        }
        val api = MessagesApi(client)

        val result = api.toggleReaction("ws-1", "chat-1", "msg-1", "👍")

        assertEquals("/public/v2/toggleReaction", capturedPath)
        assertTrue(capturedBody.contains("\"emoji\":\"👍\""))
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(result.reactionAdded)
    }

    @Test
    fun `getMessages calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"messages":[]}""")
        }
        val api = MessagesApi(client)

        val result = api.getMessages("ws-1", "chat-1", pageSize = 10)

        assertEquals("/public/v2/getMessages", capturedPath)
        assertTrue(result.messages.isEmpty())
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"pageSize\":10"))
    }

    @Test
    fun `getById calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{
                "workspaceId":"ws-1","chatId":"chat-1","messageId":"msg-1",
                "messageType":"USER","membershipId":"m-1",
                "createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z",
                "content":{"text":"found"}
            }""")
        }
        val api = MessagesApi(client)

        val result = api.getById("ws-1", "chat-1", "msg-1")

        assertEquals("/public/v2/getMessageById", capturedPath)
        assertEquals("msg-1", result.messageId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `deleteV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"updatedAt":"2024-01-01T00:00:00Z","deletedMessageIds":["m1","m2"]}""")
        }
        val api = MessagesApi(client)

        val result = api.deleteV2("ws-1", "chat-1", listOf("m1", "m2"))

        assertEquals("/public/v2/deleteMessages", capturedPath)
        assertEquals(2, result.deletedMessageIds.size)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `editV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = MessagesApi(client)

        api.editV2("ws-1", "chat-1", "msg-1", text = "edited")

        assertEquals("/public/v2/editMessage", capturedPath)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"text\":\"edited\""))
    }

    @Test
    fun `forwardV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"messageId":"fwd-1"}""")
        }
        val api = MessagesApi(client)

        val result = api.forwardV2("ws-1", "chat-src", "chat-dst", listOf("msg-1"))

        assertEquals("/public/v2/forwardMessage", capturedPath)
        assertEquals("fwd-1", result.messageId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"sourceMessageIds\""))
    }
}
