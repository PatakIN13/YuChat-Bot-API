package ru.rt.yuchatbotapi.api

import kotlinx.serialization.Serializable
import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*

/**
 * Работа с сообщениями.
 *
 * Базовые методы ([send], [edit], [delete], [forward]) используют API v1.
 * Расширенные ([sendV2], [editV2], [deleteV2], [forwardV2], [pin], [unpin],
 * [toggleReaction], [getMessages], [getById]) используют API v2.
 */
class MessagesApi internal constructor(private val client: YuChatHttpClient) {

    // ── v1: базовые операции ──

    /**
     * Отправка сообщения (v1).
     *
     * @param workspaceId ID воркспейса
     * @param chatId ID чата
     * @param text текст сообщения (поддерживает Markdown-разметку)
     * @param fileIds список ID ранее загруженных файлов
     * @param replyTo ID сообщения, на которое отвечаем
     * @return [SendMessageResponse] с ID отправленного сообщения
     * @throws AuthenticationException при невалидном токене
     * @throws RateLimitException при превышении лимита запросов
     */
    suspend fun send(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        text: String,
        fileIds: List<String>? = null,
        replyTo: ChatMessageId? = null
    ): SendMessageResponse {
        return client.post("/public/v1/chat.message.send", SendMessageV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            markdown = text,
            fileIds = fileIds,
            replyTo = replyTo?.value
        ))
    }

    /**
     * Редактирование сообщения (v1).
     *
     * @param workspaceId ID воркспейса
     * @param chatId ID чата
     * @param messageId ID сообщения для редактирования
     * @param text новый текст сообщения
     * @return [EditMessageResponse] с временной меткой обновления
     */
    suspend fun edit(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        messageId: ChatMessageId,
        text: String
    ): EditMessageResponse {
        return client.post("/public/v1/chat.message.edit", EditMessageV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            chatMessageId = messageId.value,
            newMarkdown = text
        ))
    }

    /**
     * Удаление сообщения (v1).
     *
     * @param workspaceId ID воркспейса
     * @param chatId ID чата
     * @param messageId ID сообщения для удаления
     */
    suspend fun delete(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        messageId: ChatMessageId
    ): DeleteMessageResponse {
        return client.post("/public/v1/chat.message.delete", DeleteMessageV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            chatMessageId = messageId.value
        ))
    }

    /**
     * Пересылка сообщения (v1).
     *
     * @param workspaceId ID воркспейса
     * @param sourceChatId ID чата-источника
     * @param sourceMessageId ID пересылаемого сообщения
     * @param targetChatId ID чата-назначения
     * @param text сопроводительный текст
     */
    suspend fun forward(
        workspaceId: WorkspaceId,
        sourceChatId: ChatId,
        sourceMessageId: ChatMessageId,
        targetChatId: ChatId,
        text: String
    ): ForwardMessageResponse {
        return client.post("/public/v1/chat.message.forward", ForwardMessageV1Request(
            workspaceId = workspaceId.value,
            sourceChatId = sourceChatId.value,
            sourceChatMessageId = sourceMessageId.value,
            targetChatId = targetChatId.value,
            markdown = text
        ))
    }

    // ── v2: расширенные операции ──

    /**
     * Отправка сообщения (v2) — поддержка кнопок и расширенного формата.
     *
     * @param workspaceId ID воркспейса
     * @param chatId ID чата
     * @param text текст сообщения
     * @param fileIds список ID файлов
     * @param replyTo ID сообщения для ответа
     * @param buttonBar панель кнопок ([ButtonBar])
     * @return [SendMessageResponse] с ID отправленного сообщения
     */
    suspend fun sendV2(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        text: String? = null,
        fileIds: List<String>? = null,
        replyTo: ChatMessageId? = null,
        buttonBar: ButtonBar? = null
    ): SendMessageResponse {
        return client.post("/public/v2/sendMessage", SendMessageV2Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            text = text,
            fileIds = fileIds,
            replyTo = replyTo?.value,
            buttonBar = buttonBar
        ))
    }

    /**
     * Получение сообщений чата (v2).
     *
     * @param workspaceId ID воркспейса
     * @param chatId ID чата
     * @param pageSize максимальное количество сообщений
     * @param anchorMessageId ID сообщения-якоря для пагинации
     * @param getBefore если true — получить сообщения до якоря, иначе после
     */
    suspend fun getMessages(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        pageSize: Int? = null,
        anchorMessageId: String? = null,
        getBefore: Boolean? = null
    ): GetMessagesResponse {
        return client.post("/public/v2/getMessages", GetMessagesV2Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            pageSize = pageSize,
            anchorMessageId = anchorMessageId,
            getBefore = getBefore
        ))
    }

    /** Получение сообщения по ID (v2) */
    suspend fun getById(workspaceId: WorkspaceId, chatId: ChatId, messageId: ChatMessageId): Message {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        return client.post("/public/v2/getMessageById", Req(workspaceId.value, chatId.value, messageId.value))
    }

    /** Закрепление сообщения (v2) */
    suspend fun pin(workspaceId: WorkspaceId, chatId: ChatId, messageId: ChatMessageId) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        client.postNoContent("/public/v2/pinMessage", Req(workspaceId.value, chatId.value, messageId.value))
    }

    /** Открепление сообщения (v2) */
    suspend fun unpin(workspaceId: WorkspaceId, chatId: ChatId, messageId: ChatMessageId) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        client.postNoContent("/public/v2/unpinMessage", Req(workspaceId.value, chatId.value, messageId.value))
    }

    /** Добавление/снятие реакции (v2) */
    suspend fun toggleReaction(workspaceId: WorkspaceId, chatId: ChatId, messageId: ChatMessageId, emoji: String): ToggleReactionResponse {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String, val emoji: String)
        return client.post("/public/v2/toggleReaction", Req(workspaceId.value, chatId.value, messageId.value, emoji))
    }

    /** Пакетное удаление сообщений (v2) */
    suspend fun deleteV2(workspaceId: WorkspaceId, chatId: ChatId, messageIds: List<ChatMessageId>): DeleteMessagesResponse {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageIds: List<String>)
        return client.post("/public/v2/deleteMessages", Req(workspaceId.value, chatId.value, messageIds.map { it.value }))
    }

    /** Редактирование сообщения (v2) */
    suspend fun editV2(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        messageId: ChatMessageId,
        text: String? = null,
        fileIds: List<String>? = null,
        buttonBar: ButtonBar? = null
    ) {
        client.postNoContent("/public/v2/editMessage", EditMessageV2Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            messageId = messageId.value,
            text = text,
            fileIds = fileIds,
            buttonBar = buttonBar
        ))
    }

    /** Пересылка сообщения (v2) */
    suspend fun forwardV2(
        workspaceId: WorkspaceId,
        sourceChatId: ChatId,
        targetChatId: ChatId,
        sourceMessageIds: List<ChatMessageId>,
        text: String? = null
    ): SendMessageResponse {
        return client.post("/public/v2/forwardMessage", ForwardMessageV2Request(
            workspaceId = workspaceId.value,
            sourceChatId = sourceChatId.value,
            targetChatId = targetChatId.value,
            sourceMessageIds = sourceMessageIds.map { it.value },
            text = text
        ))
    }
}

// ── Request DTOs ──

@Serializable
internal data class SendMessageV1Request(
    val workspaceId: String,
    val chatId: String,
    val markdown: String,
    val fileIds: List<String>? = null,
    val replyTo: String? = null
)

/** Ответ на отправку сообщения. */
@Serializable
data class SendMessageResponse(
    @get:JvmName("getMessageId") val messageId: ChatMessageId
)

@Serializable
internal data class EditMessageV1Request(
    val workspaceId: String,
    val chatId: String,
    val chatMessageId: String,
    val newMarkdown: String
)

/** Ответ на редактирование сообщения (v1). */
@Serializable
data class EditMessageResponse(val updatedAt: String? = null)

@Serializable
internal data class DeleteMessageV1Request(
    val workspaceId: String,
    val chatId: String,
    val chatMessageId: String
)

/** Ответ на удаление сообщения (v1). */
@Serializable
data class DeleteMessageResponse(val updatedAt: String? = null)

@Serializable
internal data class ForwardMessageV1Request(
    val workspaceId: String,
    val sourceChatId: String,
    val sourceChatMessageId: String,
    val targetChatId: String,
    val markdown: String
)

/** Ответ на пересылку сообщения (v1). */
@Serializable
data class ForwardMessageResponse(@get:JvmName("getMessageId") val messageId: ChatMessageId? = null)

@Serializable
internal data class SendMessageV2Request(
    val workspaceId: String,
    val chatId: String,
    val text: String? = null,
    val fileIds: List<String>? = null,
    val replyTo: String? = null,
    val buttonBar: ButtonBar? = null
)

@Serializable
internal data class GetMessagesV2Request(
    val workspaceId: String,
    val chatId: String,
    val pageSize: Int? = null,
    val anchorMessageId: String? = null,
    val getBefore: Boolean? = null
)

@Serializable
internal data class EditMessageV2Request(
    val workspaceId: String,
    val chatId: String,
    val messageId: String,
    val text: String? = null,
    val fileIds: List<String>? = null,
    val buttonBar: ButtonBar? = null
)

@Serializable
internal data class ForwardMessageV2Request(
    val workspaceId: String,
    val sourceChatId: String,
    val targetChatId: String,
    val sourceMessageIds: List<String>,
    val text: String? = null
)
