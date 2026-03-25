package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdatesApiMockTest {

    @Test
    fun `getUpdates v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""[
                {"updateId":1,"newChatMessage":{"workspaceId":"ws","chatId":"c1","messageId":"m1","author":"a1","markdown":"hello","createdAt":"2024-01-01T00:00:00Z"}},
                {"updateId":2,"inviteToChat":{"workspaceId":"ws","chatId":"c2","inviter":"a2"}}
            ]""")
        }
        val api = UpdatesApi(client)

        val result = api.getUpdates(offset = 0, limit = 10)

        assertEquals("/public/v1/bot.getUpdates", capturedPath)
        assertEquals(2, result.size)
        assertEquals(1L, result[0].updateId)
        assertEquals("hello", result[0].newChatMessage?.text)
        assertEquals(ChatId("c2"), result[1].inviteToChat?.chatId)
    }

    @Test
    fun `getUpdatesV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"updates":[
                {"updateId":100,"message":{
                    "workspaceId":"ws","chatId":"c1","messageId":"m1",
                    "messageType":"USER","membershipId":"mb1",
                    "createdAt":"2024-01-01","updatedAt":"2024-01-01",
                    "content":{"text":"v2 msg"}
                }}
            ]}""")
        }
        val api = UpdatesApi(client)

        val result = api.getUpdatesV2(offset = 99)

        assertEquals("/public/v2/getUpdates", capturedPath)
        assertEquals(1, result.size)
        assertEquals(100L, result[0].updateId)
        assertEquals("v2 msg", result[0].message?.content?.text)
    }

    @Test
    fun `setUpdateSettings calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = UpdatesApi(client)

        api.setUpdateSettings(
            updateSettings = listOf(
                ru.rt.yuchatbotapi.model.UpdateSetting.MESSAGE,
                ru.rt.yuchatbotapi.model.UpdateSetting.NOTIFICATION
            ),
            updateApiVersion = 2,
            autoAcceptWorkspaceInvites = true
        )

        assertEquals("/public/v2/setUpdateSettings", capturedPath)
        assertTrue(capturedBody.contains("\"updateApiVersion\":2"))
        assertTrue(capturedBody.contains("\"autoAcceptWorkspaceInvites\":true"))
    }

    @Test
    fun `getWorkspaceInvites calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"workspaceInvites":[
                {"workspaceId":"ws-new","inviterName":"John","inviterEmail":"john@corp.ru"}
            ]}""")
        }
        val api = UpdatesApi(client)

        val result = api.getWorkspaceInvites()

        assertEquals("/public/v2/getMyWorkspaceInvites", capturedPath)
        assertEquals(1, result.size)
        assertEquals(WorkspaceId("ws-new"), result[0].workspaceId)
    }

    @Test
    fun `acceptWorkspaceInvite calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = UpdatesApi(client)

        api.acceptWorkspaceInvite(WorkspaceId("ws-new"))

        assertEquals("/public/v2/acceptWorkspaceInvite", capturedPath)
    }

    @Test
    fun `rejectAllWorkspaceInvites calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = UpdatesApi(client)

        api.rejectAllWorkspaceInvites()

        assertEquals("/public/v2/rejectAllWorkspaceInvites", capturedPath)
    }
}
