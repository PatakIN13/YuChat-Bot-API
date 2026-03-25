package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatsApiMockTest {

    @Test
    fun `createWorkspace calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"chatId":"new-chat-1"}""")
        }
        val api = ChatsApi(client)

        val result = api.createWorkspace(WorkspaceId("ws-1"), name = "Test Chat", description = "Desc")

        assertEquals("/public/v1/chat.workspace.create", capturedPath)
        assertEquals(ChatId("new-chat-1"), result.chatId)
        assertTrue(capturedBody.contains("\"name\":\"Test Chat\""))
    }

    @Test
    fun `createPersonal calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chatId":"personal-1"}""")
        }
        val api = ChatsApi(client)

        val result = api.createPersonal(WorkspaceId("ws-1"), MembershipId("user-1"))

        assertEquals("/public/v1/chat.personal.create", capturedPath)
        assertEquals(ChatId("personal-1"), result.chatId)
    }

    @Test
    fun `createThread calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chatId":"thread-1"}""")
        }
        val api = ChatsApi(client)

        api.createThread(WorkspaceId("ws-1"), ChatId("chat-1"), "msg-1")

        assertEquals("/public/v1/chat.thread.create", capturedPath)
    }

    @Test
    fun `listWorkspace calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"workspaceChats":[{"chatId":"c1"},{"chatId":"c2"}]}""")
        }
        val api = ChatsApi(client)

        val result = api.listWorkspace(WorkspaceId("ws-1"))

        assertEquals("/public/v1/chat.workspace.list", capturedPath)
        assertEquals(2, result.workspaceChats?.size)
    }

    @Test
    fun `invite v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chat":{"chatId":"chat-1"}}""")
        }
        val api = ChatsApi(client)

        api.invite(WorkspaceId("ws-1"), ChatId("chat-1"), listOf(MembershipId("user-1")))

        assertEquals("/public/v1/chat.invite", capturedPath)
    }

    @Test
    fun `kick v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chat":{"chatId":"chat-1"}}""")
        }
        val api = ChatsApi(client)

        api.kick(WorkspaceId("ws-1"), ChatId("chat-1"), listOf(MembershipId("user-1")))

        assertEquals("/public/v1/chat.kick", capturedPath)
    }

    @Test
    fun `getWorkspaceChats calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"chatIds":["c1","c2","c3"]}""")
        }
        val api = ChatsApi(client)

        val result = api.getWorkspaceChats(WorkspaceId("ws-1"), pageSize = 10)

        assertEquals("/public/v2/getWorkspaceChats", capturedPath)
        assertEquals(3, result.chatIds.size)
        assertTrue(capturedBody.contains("\"pageSize\":10"))
    }

    @Test
    fun `getMyChats calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chatIds":[]}""")
        }
        val api = ChatsApi(client)

        val result = api.getMyChats(WorkspaceId("ws-1"))

        assertEquals("/public/v2/getMyWorkspaceChats", capturedPath)
        assertTrue(result.chatIds.isEmpty())
    }

    @Test
    fun `getInfo calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"chatMemberships":[]}""")
        }
        val api = ChatsApi(client)

        api.getInfo(WorkspaceId("ws-1"), listOf(ChatId("chat-1")))

        assertEquals("/public/v2/getChatsInfo", capturedPath)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `leave calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = ChatsApi(client)

        api.leave(WorkspaceId("ws-1"), ChatId("chat-1"))
        assertEquals("/public/v2/leaveChat", capturedPath)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `archive calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = ChatsApi(client)

        api.archive(WorkspaceId("ws-1"), ChatId("chat-1"))
        assertEquals("/public/v2/archiveChat", capturedPath)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }

    @Test
    fun `unarchive calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = ChatsApi(client)

        api.unarchive(WorkspaceId("ws-1"), ChatId("chat-1"))

        assertEquals("/public/v2/unrchiveChat", capturedPath)
    }

    @Test
    fun `setMemberRole calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = ChatsApi(client)

        api.setMemberRole(WorkspaceId("ws-1"), ChatId("chat-1"), MembershipId("member-1"), ru.rt.yuchatbotapi.model.ChatRole.ADMIN)

        assertEquals("/public/v2/setChatMemberRole", capturedPath)
        assertTrue(capturedBody.contains("\"membershipId\":\"member-1\""))
    }

    @Test
    fun `inviteV2 calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = ChatsApi(client)

        api.inviteV2(WorkspaceId("ws-1"), ChatId("chat-1"), listOf(MembershipId("m1"), MembershipId("m2")))

        assertEquals("/public/v2/inviteToChat", capturedPath)
        assertTrue(capturedBody.contains("\"membershipIds\""))
    }

    @Test
    fun `createUserEventsChat calls correct v2 endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"chatId":"events-1"}""")
        }
        val api = ChatsApi(client)

        val result = api.createUserEventsChat(WorkspaceId("ws-1"))

        assertEquals("/public/v2/getOrCreateUserEventsChat", capturedPath)
        assertEquals(ChatId("events-1"), result.chatId)
    }
}
