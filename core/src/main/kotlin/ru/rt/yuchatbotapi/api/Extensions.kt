package ru.rt.yuchatbotapi.api

import ru.rt.yuchatbotapi.model.*

/**
 * Текст сообщения v2 (shortcut для `content.text`).
 */
val Message.text: String? get() = content.text

/**
 * Проверяет, является ли текст сообщения v1 командой.
 *
 * @param command конкретная команда для проверки (без `/`), или `null` для проверки любой команды
 */
fun NewChatMessage.isCommand(command: String? = null): Boolean {
    val trimmed = text.trimStart()
    if (!trimmed.startsWith("/")) return false
    if (command == null) return true
    val cmd = trimmed.split("\\s+".toRegex()).firstOrNull()?.removePrefix("/") ?: return false
    return cmd == command
}

/**
 * Ответ на сообщение v1 (reply с цитированием).
 */
suspend fun NewChatMessage.reply(
    client: YuChatBotClient,
    text: String,
    fileIds: List<String>? = null
): SendMessageResponse = client.messages.send(
    workspaceId = workspaceId,
    chatId = chatId,
    text = text,
    fileIds = fileIds,
    replyTo = messageId
)

/**
 * Отправка сообщения в тот же чат v1 (без цитирования).
 */
suspend fun NewChatMessage.answer(
    client: YuChatBotClient,
    text: String,
    fileIds: List<String>? = null
): SendMessageResponse = client.messages.send(
    workspaceId = workspaceId,
    chatId = chatId,
    text = text,
    fileIds = fileIds
)

/**
 * Ответ на сообщение v2 (reply с цитированием).
 */
suspend fun Message.reply(
    client: YuChatBotClient,
    text: String? = null,
    fileIds: List<String>? = null,
    buttonBar: ButtonBar? = null
): SendMessageResponse = client.messages.sendV2(
    workspaceId = workspaceId,
    chatId = chatId,
    text = text,
    fileIds = fileIds,
    replyTo = messageId,
    buttonBar = buttonBar
)

/**
 * Отправка сообщения в тот же чат v2 (без цитирования).
 */
suspend fun Message.answer(
    client: YuChatBotClient,
    text: String? = null,
    fileIds: List<String>? = null,
    buttonBar: ButtonBar? = null
): SendMessageResponse = client.messages.sendV2(
    workspaceId = workspaceId,
    chatId = chatId,
    text = text,
    fileIds = fileIds,
    buttonBar = buttonBar
)
