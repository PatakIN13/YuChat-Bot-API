package ru.rt.yuchatbotapi.handler

import ru.rt.yuchatbotapi.model.*

/**
 * DSL-билдер для регистрации обработчиков обновлений.
 *
 * Обработчики получают распакованные типы (не обёртку Update).
 * Для доступа к сырому обновлению используйте [onUpdate] / [onUpdateV2].
 *
 * Команды (начинающиеся с `/`) обрабатываются через [onCommand] / [onCommandV2].
 * Если команда обработана, [onMessage] / [onMessageV2] не вызывается.
 */
class UpdateDispatcher {

    var onUpdateV1: (suspend (UpdateV1) -> Unit)? = null
    var onMessageV1: (suspend (NewChatMessage) -> Unit)? = null
    var onInviteV1: (suspend (InviteToChat) -> Unit)? = null
    var onJoinV1: (suspend (JoinedToChat) -> Unit)? = null
    var onLeaveV1: (suspend (LeftFromChat) -> Unit)? = null

    var onUpdateV2: (suspend (UpdateV2) -> Unit)? = null
    var onMessageV2: (suspend (Message) -> Unit)? = null
    var onNotificationV2: (suspend (Notification) -> Unit)? = null
    var onMessageActionV2: (suspend (MessageAction) -> Unit)? = null
    var onWorkspaceInviteV2: (suspend (WorkspaceInvite) -> Unit)? = null

    val commandsV1 = mutableMapOf<String, suspend (NewChatMessage, List<String>) -> Unit>()
    val commandsV2 = mutableMapOf<String, suspend (Message, List<String>) -> Unit>()

    var onError: (suspend (Throwable) -> Unit)? = null

    // ── v1 handlers ──

    /** Обработчик любого обновления (v1, сырой) */
    fun onUpdate(handler: suspend (UpdateV1) -> Unit) { onUpdateV1 = handler }

    /** Обработчик нового сообщения (v1) */
    fun onMessage(handler: suspend (NewChatMessage) -> Unit) { onMessageV1 = handler }

    /** Обработчик приглашения в чат (v1) */
    fun onInvite(handler: suspend (InviteToChat) -> Unit) { onInviteV1 = handler }

    /** Обработчик присоединения к чату (v1) */
    fun onJoin(handler: suspend (JoinedToChat) -> Unit) { onJoinV1 = handler }

    /** Обработчик выхода из чата (v1) */
    fun onLeave(handler: suspend (LeftFromChat) -> Unit) { onLeaveV1 = handler }

    /**
     * Обработчик команды (v1).
     *
     * Вызывается когда текст сообщения начинается с `/command`.
     * Если команда обработана, [onMessage] не вызывается.
     *
     * @param command имя команды (без `/`)
     * @param handler обработчик, получающий сообщение и список аргументов
     */
    fun onCommand(command: String, handler: suspend (NewChatMessage, List<String>) -> Unit) {
        commandsV1[command] = handler
    }

    // ── v2 handlers ──

    /** Обработчик любого обновления (v2, сырой) */
    fun onUpdateV2(handler: suspend (UpdateV2) -> Unit) { this.onUpdateV2 = handler }

    /** Обработчик сообщения (v2) */
    fun onMessageV2(handler: suspend (Message) -> Unit) { this.onMessageV2 = handler }

    /** Обработчик уведомления (v2) */
    fun onNotification(handler: suspend (Notification) -> Unit) { onNotificationV2 = handler }

    /** Обработчик действия с кнопкой (v2) */
    fun onMessageAction(handler: suspend (MessageAction) -> Unit) { onMessageActionV2 = handler }

    /** Обработчик приглашения в воркспейс (v2) */
    fun onWorkspaceInvite(handler: suspend (WorkspaceInvite) -> Unit) { onWorkspaceInviteV2 = handler }

    /**
     * Обработчик команды (v2).
     *
     * @param command имя команды (без `/`)
     * @param handler обработчик, получающий сообщение и список аргументов
     */
    fun onCommandV2(command: String, handler: suspend (Message, List<String>) -> Unit) {
        commandsV2[command] = handler
    }

    /** Обработчик ошибок */
    fun onError(handler: suspend (Throwable) -> Unit) { onError = handler }

    // ── dispatching ──

    suspend fun dispatchV1(update: UpdateV1) {
        onUpdateV1?.invoke(update)
        update.newChatMessage?.let { msg ->
            if (!tryDispatchCommand(msg.text, msg, commandsV1)) {
                onMessageV1?.invoke(msg)
            }
        }
        update.inviteToChat?.let { onInviteV1?.invoke(it) }
        update.joinedToChat?.let { onJoinV1?.invoke(it) }
        update.leftFromChat?.let { onLeaveV1?.invoke(it) }
    }

    suspend fun dispatchV2(update: UpdateV2) {
        onUpdateV2?.invoke(update)
        update.message?.let { msg ->
            if (!tryDispatchCommand(msg.content.text, msg, commandsV2)) {
                onMessageV2?.invoke(msg)
            }
        }
        update.notification?.let { onNotificationV2?.invoke(it) }
        update.messageAction?.let { onMessageActionV2?.invoke(it) }
        update.workspaceInvite?.let { onWorkspaceInviteV2?.invoke(it) }
    }

    private suspend fun <T> tryDispatchCommand(
        text: String?,
        payload: T,
        commands: Map<String, suspend (T, List<String>) -> Unit>
    ): Boolean {
        if (text == null || commands.isEmpty()) return false
        val trimmed = text.trimStart()
        if (!trimmed.startsWith("/")) return false
        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].removePrefix("/")
        val handler = commands[cmd] ?: return false
        handler(payload, parts.drop(1))
        return true
    }
}
