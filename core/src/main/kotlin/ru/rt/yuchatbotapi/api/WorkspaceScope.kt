package ru.rt.yuchatbotapi.api

import ru.rt.yuchatbotapi.model.*

/**
 * Scope для работы в контексте одного воркспейса.
 *
 * Убирает необходимость передавать `workspaceId` в каждый вызов.
 *
 * Пример:
 * ```kotlin
 * val ws = client.workspace("workspace-id")
 * ws.sendMessage("chat-id", "Привет!")
 * ws.listMembers()
 * ```
 */
class WorkspaceScope(val client: YuChatBotClient, val workspaceId: WorkspaceId) {

    // ── Messages v1 ──

    suspend fun sendMessage(
        chatId: ChatId, text: String,
        fileIds: List<String>? = null, replyTo: ChatMessageId? = null
    ) = client.messages.send(workspaceId, chatId, text, fileIds, replyTo)

    suspend fun editMessage(chatId: ChatId, messageId: ChatMessageId, text: String) =
        client.messages.edit(workspaceId, chatId, messageId, text)

    suspend fun deleteMessage(chatId: ChatId, messageId: ChatMessageId) =
        client.messages.delete(workspaceId, chatId, messageId)

    suspend fun forwardMessage(
        sourceChatId: ChatId, sourceMessageId: ChatMessageId,
        targetChatId: ChatId, text: String
    ) = client.messages.forward(workspaceId, sourceChatId, sourceMessageId, targetChatId, text)

    // ── Messages v2 ──

    suspend fun sendMessageV2(
        chatId: ChatId, text: String? = null,
        fileIds: List<String>? = null, replyTo: ChatMessageId? = null,
        buttonBar: ButtonBar? = null
    ) = client.messages.sendV2(workspaceId, chatId, text, fileIds, replyTo, buttonBar)

    suspend fun editMessageV2(
        chatId: ChatId, messageId: ChatMessageId,
        text: String? = null, fileIds: List<String>? = null,
        buttonBar: ButtonBar? = null
    ) = client.messages.editV2(workspaceId, chatId, messageId, text, fileIds, buttonBar)

    suspend fun deleteMessagesV2(chatId: ChatId, messageIds: List<ChatMessageId>) =
        client.messages.deleteV2(workspaceId, chatId, messageIds)

    suspend fun getMessages(
        chatId: ChatId, pageSize: Int? = null,
        anchorMessageId: String? = null, getBefore: Boolean? = null
    ) = client.messages.getMessages(workspaceId, chatId, pageSize, anchorMessageId, getBefore)

    suspend fun pinMessage(chatId: ChatId, messageId: ChatMessageId) =
        client.messages.pin(workspaceId, chatId, messageId)

    suspend fun unpinMessage(chatId: ChatId, messageId: ChatMessageId) =
        client.messages.unpin(workspaceId, chatId, messageId)

    suspend fun toggleReaction(chatId: ChatId, messageId: ChatMessageId, emoji: String) =
        client.messages.toggleReaction(workspaceId, chatId, messageId, emoji)

    // ── Chats ──

    suspend fun createChat(
        name: String? = null, type: WorkspaceChatType? = null,
        participants: List<MembershipId>? = null, announce: Boolean? = null,
        description: String? = null
    ) = client.chats.createWorkspace(workspaceId, name, type, participants, announce, description)

    suspend fun createPersonalChat(participant: MembershipId) =
        client.chats.createPersonal(workspaceId, participant)

    suspend fun listChats(chatIds: List<ChatId>? = null, maxCount: Int? = null) =
        client.chats.listWorkspace(workspaceId, chatIds, maxCount)

    suspend fun getChatsInfo(chatIds: List<ChatId>) =
        client.chats.getInfo(workspaceId, chatIds)

    // ── Members ──

    suspend fun listMembers() = client.members.list(workspaceId)

    suspend fun getMembersInfo(membershipIds: List<MembershipId>) =
        client.members.getInfo(workspaceId, membershipIds)

    // ── Files ──

    suspend fun uploadFile(
        file: java.io.File, mediaType: MediaType = MediaType.RAW,
        accessChatId: ChatId? = null
    ) = client.files.upload(workspaceId, file, mediaType, accessChatId)
}

/** Создаёт [WorkspaceScope] для указанного воркспейса. */
fun YuChatBotClient.workspace(workspaceId: WorkspaceId) = WorkspaceScope(this, workspaceId)
