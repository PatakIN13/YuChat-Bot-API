package ru.rt.yuchatbotapi.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BotSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    @Test
    fun `deserialize MeInfo`() {
        val raw = """
        {
            "profile": {
                "accountId": "a1",
                "email": "bot@yuchat.ai",
                "createdAt": "2023-01-01T00:00:00Z",
                "updatedAt": "2023-01-01T00:00:00Z",
                "fullName": "Test Bot",
                "accountType": "BOT"
            },
            "workspaces": ["w1", "w2"],
            "updateApiVersion": 1,
            "updateSettings": ["MESSAGE", "WORKSPACE_INVITE"],
            "autoAcceptWorkspaceInvite": false,
            "scope": {
                "type": "org",
                "organizationId": "org1"
            }
        }
        """.trimIndent()

        val me = json.decodeFromString<MeInfo>(raw)
        assertEquals("Test Bot", me.profile.fullName)
        assertEquals(listOf("w1", "w2"), me.workspaces)
        assertEquals(1, me.updateApiVersion)
        assertEquals(listOf(UpdateSetting.MESSAGE, UpdateSetting.WORKSPACE_INVITE), me.updateSettings)
        assertEquals(false, me.autoAcceptWorkspaceInvite)
        assertEquals(BotScopeType.ORG, me.scope.type)
        assertEquals("org1", me.scope.organizationId)
    }

    @Test
    fun `deserialize WebhookInfo`() {
        val raw = """
        {
            "url": "https://webhook.me",
            "hasCustomCertificate": true,
            "pendingUpdateCount": 5,
            "lastErrorDate": "2024-02-26T18:58:36.154+03:00",
            "lastErrorMessage": "500 Internal server error"
        }
        """.trimIndent()

        val info = json.decodeFromString<WebhookInfo>(raw)
        assertEquals("https://webhook.me", info.url)
        assertEquals(true, info.hasCustomCertificate)
        assertEquals(5, info.pendingUpdateCount)
        assertEquals("500 Internal server error", info.lastErrorMessage)
    }

    @Test
    fun `serialize SetWebhookRequest`() {
        val req = SetWebhookRequest(
            url = "https://my.bot/webhook",
            secretToken = "secret123"
        )
        val serialized = json.encodeToString(SetWebhookRequest.serializer(), req)
        assert(serialized.contains("https://my.bot/webhook"))
        assert(serialized.contains("secret123"))
        assert(!serialized.contains("certificate"))
    }

    @Test
    fun `deserialize ButtonBar`() {
        val raw = """
        {
            "buttonGroups": [
                {
                    "buttons": [
                        {"commandButton": {"displayText": "Да", "commandKey": "/yes"}},
                        {"commandButton": {"displayText": "Нет", "commandKey": "/no"}}
                    ]
                },
                {
                    "buttons": [
                        {"linkButton": {"displayText": "Документация", "link": "https://docs.example.com"}}
                    ]
                }
            ]
        }
        """.trimIndent()

        val bar = json.decodeFromString<ButtonBar>(raw)
        assertEquals(2, bar.buttonGroups.size)
        assertEquals(2, bar.buttonGroups[0].buttons.size)
        assertEquals("Да", bar.buttonGroups[0].buttons[0].commandButton!!.displayText)
        assertEquals("/yes", bar.buttonGroups[0].buttons[0].commandKey())
        assertEquals("https://docs.example.com", bar.buttonGroups[1].buttons[0].linkButton!!.link)
    }

    @Test
    fun `deserialize FileUploadResponse`() {
        val raw = """
        {
            "fileId": "BDK5NyCqIK",
            "uploadUrl": "https://yuchat-files-presigned.storage.net/d2f19302"
        }
        """.trimIndent()

        val resp = json.decodeFromString<FileUploadResponse>(raw)
        assertEquals("BDK5NyCqIK", resp.fileId)
        assert(resp.uploadUrl.startsWith("https://"))
    }

    @Test
    fun `deserialize enums`() {
        val raw = """{"type": "pub"}"""

        @kotlinx.serialization.Serializable
        data class Wrapper(val type: BotScopeType)

        val w = json.decodeFromString<Wrapper>(raw)
        assertEquals(BotScopeType.PUB, w.type)
    }
}

private fun Button.commandKey(): String? = commandButton?.commandKey
