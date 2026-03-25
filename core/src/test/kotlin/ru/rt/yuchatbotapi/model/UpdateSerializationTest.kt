package ru.rt.yuchatbotapi.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UpdateSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    @Test
    fun `deserialize v1 Update with message`() {
        val raw = """
        {
            "updateId": 42,
            "newChatMessage": {
                "workspaceId": "w1",
                "chatId": "c1",
                "messageId": "m1",
                "author": "a1",
                "markdown": "Hello",
                "createdAt": "2023-01-01T00:00:00Z"
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV1>(raw)
        assertEquals(42L, update.updateId)
        assertNotNull(update.newChatMessage)
        assertEquals("Hello", update.newChatMessage!!.text)
        assertNull(update.inviteToChat)
    }

    @Test
    fun `deserialize v1 Update with invite`() {
        val raw = """
        {
            "updateId": 43,
            "inviteToChat": {
                "workspaceId": "w1",
                "chatId": "c1",
                "inviter": "a1"
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV1>(raw)
        assertEquals(43L, update.updateId)
        assertNull(update.newChatMessage)
        assertNotNull(update.inviteToChat)
        assertEquals(AccountId("a1"), update.inviteToChat!!.inviter)
    }

    @Test
    fun `deserialize v1 Update with join`() {
        val raw = """
        {
            "updateId": 44,
            "joinedToChat": {
                "workspaceId": "w1",
                "chatId": "c1",
                "inviter": "mb1",
                "joined": ["mb2", "mb3"]
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV1>(raw)
        assertNotNull(update.joinedToChat)
        assertEquals(listOf(MembershipId("mb2"), MembershipId("mb3")), update.joinedToChat!!.joined)
    }

    @Test
    fun `deserialize v2 Update with message`() {
        val raw = """
        {
            "updateId": 100,
            "message": {
                "workspaceId": "w1",
                "chatId": "c1",
                "messageId": "m1",
                "messageType": "USER",
                "membershipId": "mb1",
                "createdAt": "2023-01-01T00:00:00Z",
                "updatedAt": "2023-01-01T00:00:00Z",
                "content": {"text": "v2 message"}
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV2>(raw)
        assertEquals(100L, update.updateId)
        assertNotNull(update.message)
        assertEquals("v2 message", update.message!!.content.text)
    }

    @Test
    fun `deserialize v2 Update with notification`() {
        val raw = """
        {
            "updateId": 101,
            "notification": {
                "workspaceId": "w1",
                "timestamp": "2024-01-01T00:00:00Z",
                "reactionToggledEvent": {
                    "chatId": "c1",
                    "messageId": "m1",
                    "emoji": ":heart:",
                    "reactedBy": "mb1",
                    "wasSet": true
                }
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV2>(raw)
        assertNotNull(update.notification)
        val event = update.notification!!.reactionToggledEvent!!
        assertEquals(":heart:", event.emoji)
        assertEquals(true, event.wasSet)
    }

    @Test
    fun `deserialize v2 Update with messageAction`() {
        val raw = """
        {
            "updateId": 102,
            "messageAction": {
                "workspaceId": "w1",
                "chatId": "c1",
                "messageId": "m1",
                "pressedButtonCommand": {
                    "displayText": "OK",
                    "commandKey": "/confirm"
                }
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV2>(raw)
        assertNotNull(update.messageAction)
        assertEquals("/confirm", update.messageAction!!.pressedButtonCommand!!.commandKey)
    }

    @Test
    fun `deserialize v2 Update with workspace invite`() {
        val raw = """
        {
            "updateId": 103,
            "workspaceInvite": {
                "workspaceId": "w1",
                "inviterName": "Иванов Иван",
                "inviterEmail": "ivanov@example.org"
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV2>(raw)
        assertNotNull(update.workspaceInvite)
        assertEquals("Иванов Иван", update.workspaceInvite!!.inviterName)
    }

    @Test
    fun `deserialize v2 notification with member events`() {
        val raw = """
        {
            "updateId": 104,
            "notification": {
                "workspaceId": "w1",
                "timestamp": "2024-01-01T00:00:00Z",
                "memberChangedRoleEvent": {
                    "memberId": "mb1",
                    "newRole": "ADMIN"
                }
            }
        }
        """.trimIndent()

        val update = json.decodeFromString<UpdateV2>(raw)
        val event = update.notification!!.memberChangedRoleEvent!!
        assertEquals(MembershipId("mb1"), event.memberId)
        assertEquals(WorkspaceRole.ADMIN, event.newRole)
    }
}
