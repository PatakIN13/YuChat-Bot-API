package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.*
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.MessagesApi}.
 */
class MessagesJavaApi internal constructor(private val api: MessagesApi) {

    // ── v1 ──

    @JvmOverloads
    fun send(
        workspaceId: String,
        chatId: String,
        text: String,
        fileIds: List<String>? = null,
        replyTo: String? = null
    ): CompletableFuture<SendMessageResponse> = async {
        api.send(WorkspaceId(workspaceId), ChatId(chatId), text, fileIds, replyTo?.let { ChatMessageId(it) })
    }

    fun edit(
        workspaceId: String,
        chatId: String,
        messageId: String,
        text: String
    ): CompletableFuture<EditMessageResponse> = async {
        api.edit(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId), text)
    }

    fun delete(
        workspaceId: String,
        chatId: String,
        messageId: String
    ): CompletableFuture<DeleteMessageResponse> = async {
        api.delete(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId))
    }

    fun forward(
        workspaceId: String,
        sourceChatId: String,
        sourceMessageId: String,
        targetChatId: String,
        text: String
    ): CompletableFuture<ForwardMessageResponse> = async {
        api.forward(WorkspaceId(workspaceId), ChatId(sourceChatId), ChatMessageId(sourceMessageId), ChatId(targetChatId), text)
    }

    // ── v2 ──

    @JvmOverloads
    fun sendV2(
        workspaceId: String,
        chatId: String,
        text: String? = null,
        fileIds: List<String>? = null,
        replyTo: String? = null,
        buttonBar: ButtonBar? = null
    ): CompletableFuture<SendMessageResponse> = async {
        api.sendV2(WorkspaceId(workspaceId), ChatId(chatId), text, fileIds, replyTo?.let { ChatMessageId(it) }, buttonBar)
    }

    @JvmOverloads
    fun getMessages(
        workspaceId: String,
        chatId: String,
        pageSize: Int? = null,
        anchorMessageId: String? = null,
        getBefore: Boolean? = null
    ): CompletableFuture<GetMessagesResponse> = async {
        api.getMessages(WorkspaceId(workspaceId), ChatId(chatId), pageSize, anchorMessageId, getBefore)
    }

    fun getById(workspaceId: String, chatId: String, messageId: String): CompletableFuture<Message> = async {
        api.getById(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId))
    }

    fun pin(workspaceId: String, chatId: String, messageId: String): CompletableFuture<Void> = asyncVoid {
        api.pin(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId))
    }

    fun unpin(workspaceId: String, chatId: String, messageId: String): CompletableFuture<Void> = asyncVoid {
        api.unpin(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId))
    }

    fun toggleReaction(workspaceId: String, chatId: String, messageId: String, emoji: String): CompletableFuture<ToggleReactionResponse> = async {
        api.toggleReaction(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId), emoji)
    }

    fun deleteV2(workspaceId: String, chatId: String, messageIds: List<String>): CompletableFuture<DeleteMessagesResponse> = async {
        api.deleteV2(WorkspaceId(workspaceId), ChatId(chatId), messageIds.map { ChatMessageId(it) })
    }

    @JvmOverloads
    fun editV2(
        workspaceId: String,
        chatId: String,
        messageId: String,
        text: String? = null,
        fileIds: List<String>? = null,
        buttonBar: ButtonBar? = null
    ): CompletableFuture<Void> = asyncVoid {
        api.editV2(WorkspaceId(workspaceId), ChatId(chatId), ChatMessageId(messageId), text, fileIds, buttonBar)
    }

    @JvmOverloads
    fun forwardV2(
        workspaceId: String,
        sourceChatId: String,
        targetChatId: String,
        sourceMessageIds: List<String>,
        text: String? = null
    ): CompletableFuture<SendMessageResponse> = async {
        api.forwardV2(WorkspaceId(workspaceId), ChatId(sourceChatId), ChatId(targetChatId), sourceMessageIds.map { ChatMessageId(it) }, text)
    }
}
