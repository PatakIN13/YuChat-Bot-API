package ru.rt.yuchatbotapi.polling

import ru.rt.yuchatbotapi.model.UpdateV1
import ru.rt.yuchatbotapi.model.UpdateV2

/**
 * DSL-билдер для регистрации обработчиков обновлений.
 */
class UpdateDispatcher {

    internal var onUpdateV1: (suspend (UpdateV1) -> Unit)? = null
    internal var onMessageV1: (suspend (UpdateV1) -> Unit)? = null
    internal var onInviteV1: (suspend (UpdateV1) -> Unit)? = null
    internal var onJoinV1: (suspend (UpdateV1) -> Unit)? = null
    internal var onLeaveV1: (suspend (UpdateV1) -> Unit)? = null

    internal var onUpdateV2: (suspend (UpdateV2) -> Unit)? = null
    internal var onMessageV2: (suspend (UpdateV2) -> Unit)? = null
    internal var onNotificationV2: (suspend (UpdateV2) -> Unit)? = null
    internal var onMessageActionV2: (suspend (UpdateV2) -> Unit)? = null
    internal var onWorkspaceInviteV2: (suspend (UpdateV2) -> Unit)? = null

    internal var onError: (suspend (Throwable) -> Unit)? = null

    // ── v1 handlers ──

    /** Обработчик любого обновления (v1) */
    fun onUpdate(handler: suspend (UpdateV1) -> Unit) { onUpdateV1 = handler }

    /** Обработчик нового сообщения (v1) */
    fun onMessage(handler: suspend (UpdateV1) -> Unit) { onMessageV1 = handler }

    /** Обработчик приглашения в чат (v1) */
    fun onInvite(handler: suspend (UpdateV1) -> Unit) { onInviteV1 = handler }

    /** Обработчик присоединения к чату (v1) */
    fun onJoin(handler: suspend (UpdateV1) -> Unit) { onJoinV1 = handler }

    /** Обработчик выхода из чата (v1) */
    fun onLeave(handler: suspend (UpdateV1) -> Unit) { onLeaveV1 = handler }

    // ── v2 handlers ──

    /** Обработчик любого обновления (v2) */
    fun onUpdateV2(handler: suspend (UpdateV2) -> Unit) { this.onUpdateV2 = handler }

    /** Обработчик сообщения (v2) */
    fun onMessageV2(handler: suspend (UpdateV2) -> Unit) { this.onMessageV2 = handler }

    /** Обработчик уведомления (v2) */
    fun onNotification(handler: suspend (UpdateV2) -> Unit) { onNotificationV2 = handler }

    /** Обработчик действия с сообщением (v2) */
    fun onMessageAction(handler: suspend (UpdateV2) -> Unit) { onMessageActionV2 = handler }

    /** Обработчик приглашения в воркспейс (v2) */
    fun onWorkspaceInvite(handler: suspend (UpdateV2) -> Unit) { onWorkspaceInviteV2 = handler }

    /** Обработчик ошибок */
    fun onError(handler: suspend (Throwable) -> Unit) { onError = handler }

    // ── dispatching ──

    internal suspend fun dispatchV1(update: UpdateV1) {
        onUpdateV1?.invoke(update)
        when {
            update.newChatMessage != null -> onMessageV1?.invoke(update)
            update.inviteToChat != null -> onInviteV1?.invoke(update)
            update.joinedToChat != null -> onJoinV1?.invoke(update)
            update.leftFromChat != null -> onLeaveV1?.invoke(update)
        }
    }

    internal suspend fun dispatchV2(update: UpdateV2) {
        onUpdateV2?.invoke(update)
        when {
            update.message != null -> onMessageV2?.invoke(update)
            update.notification != null -> onNotificationV2?.invoke(update)
            update.messageAction != null -> onMessageActionV2?.invoke(update)
            update.workspaceInvite != null -> onWorkspaceInviteV2?.invoke(update)
        }
    }
}
