package ru.rt.yuchatbotapi.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    @Test
    fun `deserialize WorkspaceChat`() {
        val raw = """
        {
            "chatId": "c56Y0kgi",
            "workspaceId": "w67Y89gu",
            "name": "Флудилка",
            "type": "PUBLIC",
            "announceChannel": false,
            "description": "Общение",
            "membershipIds": ["a1", "a2"]
        }
        """.trimIndent()

        val chat = json.decodeFromString<WorkspaceChat>(raw)
        assertEquals("c56Y0kgi", chat.chatId)
        assertEquals(WorkspaceChatType.PUBLIC, chat.type)
        assertEquals(false, chat.announceChannel)
        assertEquals(2, chat.membershipIds?.size)
    }

    @Test
    fun `deserialize ChatMembership v2`() {
        val raw = """
        {
            "chatId": "w:AIQffAsGia",
            "workspaceId": "AIQffAsGi8",
            "metadata": {
                "workspace": {
                    "name": "Новости",
                    "announce": true,
                    "chatType": "PUBLIC",
                    "autoJoinNewMembers": true,
                    "memberCount": 5,
                    "description": "Новости компании"
                }
            },
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-06-01T00:00:00Z",
            "chatRole": "ADMIN",
            "chatPermissions": ["RENAME_CHAT", "KICK_FROM_CHAT"]
        }
        """.trimIndent()

        val chat = json.decodeFromString<ChatMembership>(raw)
        assertEquals("w:AIQffAsGia", chat.chatId)
        assertEquals(ChatRole.ADMIN, chat.chatRole)
        val ws = chat.metadata.workspace!!
        assertEquals("Новости", ws.name)
        assertEquals(true, ws.announce)
        assertEquals(5, ws.memberCount)
        assertNull(chat.metadata.personal)
    }

    @Test
    fun `deserialize ChatMetadata personal`() {
        val raw = """
        {
            "chatId": "p:abc",
            "workspaceId": "w1",
            "metadata": {
                "personal": {"otherMembershipId": "mb2"}
            },
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-01-01T00:00:00Z"
        }
        """.trimIndent()

        val chat = json.decodeFromString<ChatMembership>(raw)
        assertEquals("mb2", chat.metadata.personal!!.otherMembershipId)
        assertNull(chat.metadata.workspace)
    }

    @Test
    fun `deserialize ChatMetadata thread`() {
        val raw = """
        {
            "chatId": "t:abc",
            "workspaceId": "w1",
            "metadata": {
                "thread": {
                    "parentChatId": "w:parent",
                    "parentMessageId": "msg1"
                }
            },
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-01-01T00:00:00Z"
        }
        """.trimIndent()

        val chat = json.decodeFromString<ChatMembership>(raw)
        assertEquals("w:parent", chat.metadata.thread!!.parentChatId)
        assertEquals("msg1", chat.metadata.thread!!.parentMessageId)
    }
}
