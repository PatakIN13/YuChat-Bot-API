package ru.rt.yuchatbotapi.handler

import kotlinx.coroutines.test.runTest
import ru.rt.yuchatbotapi.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateDispatcherSelfFilterTest {

    private fun newChatMessage(author: String, text: String = "hello") = NewChatMessage(
        workspaceId = WorkspaceId("w1"),
        chatId = ChatId("c1"),
        messageId = ChatMessageId("m1"),
        author = AccountId(author),
        text = text,
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun v1Update(msg: NewChatMessage) = UpdateV1(updateId = 1, newChatMessage = msg)

    private fun v2Message(membershipId: String, text: String = "hello") = Message(
        workspaceId = WorkspaceId("w1"),
        chatId = ChatId("c1"),
        messageId = ChatMessageId("m1"),
        messageType = MessageType.USER,
        membershipId = MembershipId(membershipId),
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        content = MessageContent(text = text)
    )

    private fun v2Update(msg: Message) = UpdateV2(updateId = 1, message = msg)

    // ── v1 ──

    @Test
    fun `v1 - ignores self-message when botAccountId is set`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botAccountId = AccountId("bot-account")

        val received = mutableListOf<NewChatMessage>()
        dispatcher.onMessage { received.add(it) }

        dispatcher.dispatchV1(v1Update(newChatMessage(author = "bot-account")))
        assertTrue(received.isEmpty(), "Self-message should be filtered")
    }

    @Test
    fun `v1 - passes message from other user`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botAccountId = AccountId("bot-account")

        val received = mutableListOf<NewChatMessage>()
        dispatcher.onMessage { received.add(it) }

        dispatcher.dispatchV1(v1Update(newChatMessage(author = "user-account")))
        assertEquals(1, received.size)
    }

    @Test
    fun `v1 - passes all messages when botAccountId is null`() = runTest {
        val dispatcher = UpdateDispatcher()

        val received = mutableListOf<NewChatMessage>()
        dispatcher.onMessage { received.add(it) }

        dispatcher.dispatchV1(v1Update(newChatMessage(author = "any-account")))
        assertEquals(1, received.size)
    }

    @Test
    fun `v1 - raw onUpdate still receives self-messages`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botAccountId = AccountId("bot-account")

        val rawReceived = mutableListOf<UpdateV1>()
        val msgReceived = mutableListOf<NewChatMessage>()
        dispatcher.onUpdate { rawReceived.add(it) }
        dispatcher.onMessage { msgReceived.add(it) }

        dispatcher.dispatchV1(v1Update(newChatMessage(author = "bot-account")))
        assertEquals(1, rawReceived.size, "Raw handler should receive self-message")
        assertTrue(msgReceived.isEmpty(), "Typed handler should not receive self-message")
    }

    @Test
    fun `v1 - self-message command is also filtered`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botAccountId = AccountId("bot-account")

        val cmdReceived = mutableListOf<String>()
        dispatcher.onCommand("help") { _, _ -> cmdReceived.add("help") }

        dispatcher.dispatchV1(v1Update(newChatMessage(author = "bot-account", text = "/help")))
        assertTrue(cmdReceived.isEmpty(), "Self-message command should be filtered")
    }

    // ── v2 ──

    @Test
    fun `v2 - ignores self-message when botMembershipIds contains author`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = setOf(MembershipId("bot-member"))

        val received = mutableListOf<Message>()
        dispatcher.onMessageV2 { received.add(it) }

        dispatcher.dispatchV2(v2Update(v2Message(membershipId = "bot-member")))
        assertTrue(received.isEmpty(), "Self-message should be filtered")
    }

    @Test
    fun `v2 - passes message from other member`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = setOf(MembershipId("bot-member"))

        val received = mutableListOf<Message>()
        dispatcher.onMessageV2 { received.add(it) }

        dispatcher.dispatchV2(v2Update(v2Message(membershipId = "user-member")))
        assertEquals(1, received.size)
    }

    @Test
    fun `v2 - raw onUpdateV2 still receives self-messages`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = setOf(MembershipId("bot-member"))

        val rawReceived = mutableListOf<UpdateV2>()
        val msgReceived = mutableListOf<Message>()
        dispatcher.onUpdateV2 { rawReceived.add(it) }
        dispatcher.onMessageV2 { msgReceived.add(it) }

        dispatcher.dispatchV2(v2Update(v2Message(membershipId = "bot-member")))
        assertEquals(1, rawReceived.size, "Raw handler should receive self-message")
        assertTrue(msgReceived.isEmpty(), "Typed handler should not receive self-message")
    }

    @Test
    fun `v2 - self-message command is also filtered`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = setOf(MembershipId("bot-member"))

        val cmdReceived = mutableListOf<String>()
        dispatcher.onCommandV2("help") { _, _ -> cmdReceived.add("help") }

        dispatcher.dispatchV2(v2Update(v2Message(membershipId = "bot-member", text = "/help")))
        assertTrue(cmdReceived.isEmpty(), "Self-message command should be filtered")
    }

    // ── workspace invite resolver ──

    @Test
    fun `v2 - resolves membershipId on workspace invite`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = emptySet()
        dispatcher.membershipResolver = { MembershipId("bot-in-${it.value}") }

        val invites = mutableListOf<WorkspaceInvite>()
        dispatcher.onWorkspaceInvite { invites.add(it) }

        val invite = WorkspaceInvite(
            workspaceId = WorkspaceId("new-ws"),
            inviterName = "User",
            inviterEmail = "user@example.com"
        )
        dispatcher.dispatchV2(UpdateV2(updateId = 1, workspaceInvite = invite))

        assertEquals(1, invites.size, "User handler should still receive invite")
        assertTrue(
            dispatcher.botMembershipIds.contains(MembershipId("bot-in-new-ws")),
            "Bot membershipId should be resolved for new workspace"
        )
    }

    @Test
    fun `v2 - filters messages after workspace invite resolved`() = runTest {
        val dispatcher = UpdateDispatcher()
        dispatcher.botMembershipIds = emptySet()
        dispatcher.membershipResolver = { MembershipId("bot-in-${it.value}") }

        // Simulate invite
        val invite = WorkspaceInvite(
            workspaceId = WorkspaceId("new-ws"),
            inviterName = "User",
            inviterEmail = "user@example.com"
        )
        dispatcher.dispatchV2(UpdateV2(updateId = 1, workspaceInvite = invite))

        // Now a self-message from the new workspace should be filtered
        val received = mutableListOf<Message>()
        dispatcher.onMessageV2 { received.add(it) }

        dispatcher.dispatchV2(v2Update(v2Message(membershipId = "bot-in-new-ws")))
        assertTrue(received.isEmpty(), "Self-message from new workspace should be filtered after invite")
    }
}
