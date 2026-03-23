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
        workspaceId: String,
        chatId: String,
        text: String,
        fileIds: List<String>? = null,
        replyTo: String? = null
    ): SendMessageResponse {
        return client.post("/public/v1/chat.message.send", SendMessageV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            markdown = text,
            fileIds = fileIds,
            replyTo = replyTo
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
        workspaceId: String,
        chatId: String,
        messageId: String,
        text: String
    ): EditMessageResponse {
        return client.post("/public/v1/chat.message.edit", EditMessageV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            chatMessageId = messageId,
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
        workspaceId: String,
        chatId: String,
        messageId: String
    ): DeleteMessageResponse {
        return client.post("/public/v1/chat.message.delete", DeleteMessageV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            chatMessageId = messageId
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
        workspaceId: String,
        sourceChatId: String,
        sourceMessageId: String,
        targetChatId: String,
        text: String
    ): ForwardMessageResponse {
        return client.post("/public/v1/chat.message.forward", ForwardMessageV1Request(
            workspaceId = workspaceId,
            sourceChatId = sourceChatId,
            sourceChatMessageId = sourceMessageId,
            targetChatId = targetChatId,
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
        workspaceId: String,
        chatId: String,
        text: String? = null,
        fileIds: List<String>? = null,
        replyTo: String? = null,
        buttonBar: ButtonBar? = null
    ): SendMessageResponse {
        return client.post("/public/v2/sendMessage", SendMessageV2Request(
            workspaceId = workspaceId,
            chatId = chatId,
            text = text,
            fileIds = fileIds,
            replyTo = replyTo,
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
        workspaceId: String,
        chatId: String,
        pageSize: Int? = null,
        anchorMessageId: String? = null,
        getBefore: Boolean? = null
    ): GetMessagesResponse {
        return client.post("/public/v2/getMessages", GetMessagesV2Request(
            workspaceId = workspaceId,
            chatId = chatId,
            pageSize = pageSize,
            anchorMessageId = anchorMessageId,
            getBefore = getBefore
        ))
    }

    /** Получение сообщения по ID (v2) */
    suspend fun getById(workspaceId: String, chatId: String, messageId: String): Message {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        return client.post("/public/v2/getMessageById", Req(workspaceId, chatId, messageId))
    }

    /** Закрепление сообщения (v2) */
    suspend fun pin(workspaceId: String, chatId: String, messageId: String) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        client.postNoContent("/public/v2/pinMessage", Req(workspaceId, chatId, messageId))
    }

    /** Открепление сообщения (v2) */
    suspend fun unpin(workspaceId: String, chatId: String, messageId: String) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String)
        client.postNoContent("/public/v2/unpinMessage", Req(workspaceId, chatId, messageId))
    }

    /** Добавление/снятие реакции (v2) */
    suspend fun toggleReaction(workspaceId: String, chatId: String, messageId: String, emoji: String): ToggleReactionResponse {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageId: String, val emoji: String)
        return client.post("/public/v2/toggleReaction", Req(workspaceId, chatId, messageId, emoji))
    }

    /** Пакетное удаление сообщений (v2) */
    suspend fun deleteV2(workspaceId: String, chatId: String, messageIds: List<String>): DeleteMessagesResponse {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val messageIds: List<String>)
        return client.post("/public/v2/deleteMessages", Req(workspaceId, chatId, messageIds))
    }

    /** Редактирование сообщения (v2) */
    suspend fun editV2(
        workspaceId: String,
        chatId: String,
        messageId: String,
        text: String? = null,
        fileIds: List<String>? = null,
        buttonBar: ButtonBar? = null
    ) {
        client.postNoContent("/public/v2/editMessage", EditMessageV2Request(
            workspaceId = workspaceId,
            chatId = chatId,
            messageId = messageId,
            text = text,
            fileIds = fileIds,
            buttonBar = buttonBar
        ))
    }

    /** Пересылка сообщения (v2) */
    suspend fun forwardV2(
        workspaceId: String,
        sourceChatId: String,
        targetChatId: String,
        sourceMessageIds: List<String>,
        text: String? = null
    ): SendMessageResponse {
        return client.post("/public/v2/forwardMessage", ForwardMessageV2Request(
            workspaceId = workspaceId,
            sourceChatId = sourceChatId,
            targetChatId = targetChatId,
            sourceMessageIds = sourceMessageIds,
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
    val messageId: String
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
data class ForwardMessageResponse(val messageId: String? = null)

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
