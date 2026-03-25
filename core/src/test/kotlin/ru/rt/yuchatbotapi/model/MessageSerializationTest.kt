package ru.rt.yuchatbotapi.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MessageSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    @Test
    fun `deserialize NewChatMessage`() {
        val raw = """
        {
            "workspaceId": "w67Y89gu",
            "chatId": "c56Y0kgi",
            "messageId": "m56Y0kgi",
            "author": "a56Y0kgi",
            "markdown": "Привет!",
            "createdAt": "2023-02-26T18:58:36.154+03:00"
        }
        """.trimIndent()

        val msg = json.decodeFromString<NewChatMessage>(raw)
        assertEquals(WorkspaceId("w67Y89gu"), msg.workspaceId)
        assertEquals(ChatId("c56Y0kgi"), msg.chatId)
        assertEquals(ChatMessageId("m56Y0kgi"), msg.messageId)
        assertEquals(AccountId("a56Y0kgi"), msg.author)
        assertEquals("Привет!", msg.text)
        assertNull(msg.parentMessageId)
        assertNull(msg.fileIds)
    }

    @Test
    fun `deserialize NewChatMessage with optional fields`() {
        val raw = """
        {
            "workspaceId": "w1",
            "chatId": "c1",
            "messageId": "m1",
            "author": "a1",
            "markdown": "reply",
            "parentMessageId": "m0",
            "parentMessageAuthor": "a0",
            "fileIds": ["f1", "f2"],
            "createdAt": "2023-01-01T00:00:00Z"
        }
        """.trimIndent()

        val msg = json.decodeFromString<NewChatMessage>(raw)
        assertEquals(ChatMessageId("m0"), msg.parentMessageId)
        assertEquals(AccountId("a0"), msg.parentMessageAuthor)
        assertEquals(listOf("f1", "f2"), msg.fileIds)
    }

    @Test
    fun `serialize NewChatMessage omits nulls`() {
        val msg = NewChatMessage(
            workspaceId = WorkspaceId("w1"),
            chatId = ChatId("c1"),
            messageId = ChatMessageId("m1"),
            author = AccountId("a1"),
            text = "test",
            createdAt = "2024-01-01T00:00:00Z"
        )
        val serialized = json.encodeToString(NewChatMessage.serializer(), msg)
        assert(!serialized.contains("parentMessageId"))
        assert(!serialized.contains("fileIds"))
    }

    @Test
    fun `deserialize v2 Message`() {
        val raw = """
        {
            "workspaceId": "AIQffAsGi8",
            "chatId": "w:AIQffAsGi8",
            "messageId": "gIQffAsGi8",
            "messageType": "USER",
            "membershipId": "gIQffAsGi8",
            "createdAt": "2023-02-26T18:58:36.154+03:00",
            "updatedAt": "2024-02-26T18:58:36.154+03:00",
            "content": {
                "text": "Hello from v2!"
            },
            "reactions": [
                {"emoji": ":heart:", "count": 3}
            ],
            "threadId": "t:AIQffAsGi8",
            "pinnedAt": "2024-03-01T10:00:00Z"
        }
        """.trimIndent()

        val msg = json.decodeFromString<Message>(raw)
        assertEquals(WorkspaceId("AIQffAsGi8"), msg.workspaceId)
        assertEquals(MessageType.USER, msg.messageType)
        assertEquals("Hello from v2!", msg.content.text)
        assertEquals(1, msg.reactions?.size)
        assertEquals(":heart:", msg.reactions!![0].emoji)
        assertEquals(3, msg.reactions!![0].count)
        assertEquals("t:AIQffAsGi8", msg.threadId)
    }

    @Test
    fun `deserialize v2 Message with forwarded content`() {
        val raw = """
        {
            "workspaceId": "w1",
            "chatId": "c1",
            "messageId": "m1",
            "messageType": "USER",
            "membershipId": "mb1",
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-01-01T00:00:00Z",
            "content": {
                "forwardedContent": {
                    "sourceWorkspaceId": "w0",
                    "sourceChatId": "c0",
                    "forwardedMessages": [
                        {
                            "workspaceId": "w0",
                            "chatId": "c0",
                            "messageId": "m0",
                            "messageType": "USER",
                            "membershipId": "mb0",
                            "createdAt": "2022-01-01T00:00:00Z",
                            "updatedAt": "2022-01-01T00:00:00Z",
                            "content": {"text": "Original message"}
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val msg = json.decodeFromString<Message>(raw)
        val fwd = msg.content.forwardedContent!!
        assertEquals(WorkspaceId("w0"), fwd.sourceWorkspaceId)
        assertEquals(1, fwd.forwardedMessages.size)
        assertEquals("Original message", fwd.forwardedMessages[0].content.text)
    }

    @Test
    fun `deserialize v2 Message with system event`() {
        val raw = """
        {
            "workspaceId": "w1",
            "chatId": "c1",
            "messageId": "m1",
            "messageType": "SYSTEM",
            "membershipId": "mb1",
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-01-01T00:00:00Z",
            "content": {
                "systemEvent": {
                    "chatCreated": {
                        "creatorMembershipId": "mb1",
                        "membershipIds": ["mb1", "mb2"]
                    }
                }
            }
        }
        """.trimIndent()

        val msg = json.decodeFromString<Message>(raw)
        assertEquals(MessageType.SYSTEM, msg.messageType)
        val event = msg.content.systemEvent!!.chatCreated!!
        assertEquals(MembershipId("mb1"), event.creatorMembershipId)
        assertEquals(listOf(MembershipId("mb1"), MembershipId("mb2")), event.membershipIds)
    }
}
