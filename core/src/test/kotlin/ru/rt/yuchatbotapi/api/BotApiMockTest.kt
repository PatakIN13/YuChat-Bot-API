package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class BotApiMockTest {

    @Test
    fun `getMe calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{
                "profile":{
                    "accountId":"bot-acc-1",
                    "createdAt":"2024-01-01T00:00:00Z",
                    "updatedAt":"2024-01-01T00:00:00Z",
                    "accountType":"BOT",
                    "fullName":"TestBot"
                },
                "workspaces":["ws-1","ws-2"],
                "updateApiVersion":2,
                "updateSettings":["MESSAGE","NOTIFICATION"],
                "autoAcceptWorkspaceInvite":false,
                "scope":{"type":"pub"}
            }""")
        }
        val api = BotApi(client)

        val result = api.getMe()

        assertEquals("/public/v2/getMe", capturedPath)
        assertEquals("bot-acc-1", result.profile.accountId)
        assertEquals("TestBot", result.profile.fullName)
        assertEquals(2, result.workspaces.size)
        assertEquals(2, result.updateApiVersion)
        assertEquals(false, result.autoAcceptWorkspaceInvite)
        assertEquals(ru.rt.yuchatbotapi.model.BotScopeType.PUB, result.scope.type)
    }
}
