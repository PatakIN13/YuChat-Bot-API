package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

// ── v1 Update ──

/**
 * Обновление (v1).
 *
 * Содержит одно из событий: новое сообщение, приглашение, присоединение или выход.
 * Получается через [ru.rt.yuchatbotapi.api.UpdatesApi.getUpdates] или webhook.
 *
 * @property updateId последовательный ID обновления (используется как offset)
 */
@Serializable
data class UpdateV1(
    val updateId: Long,
    val newChatMessage: NewChatMessage? = null,
    val inviteToChat: InviteToChat? = null,
    val joinedToChat: JoinedToChat? = null,
    val leftFromChat: LeftFromChat? = null
)

// ── v2 Update ──

/**
 * Обновление (v2).
 *
 * Расширенная модель обновления с поддержкой уведомлений, действий с кнопками
 * и приглашений в воркспейс.
 *
 * Для получения обновлений v2 необходимо предварительно вызвать
 * [ru.rt.yuchatbotapi.api.UpdatesApi.setUpdateSettings] с `updateApiVersion = 2`.
 *
 * @property updateId последовательный ID обновления
 * @property message новое сообщение (если [UpdateSetting.MESSAGE] включён)
 * @property notification уведомление о событии (если [UpdateSetting.NOTIFICATION] включён)
 * @property messageAction нажатие кнопки в сообщении (если [UpdateSetting.MESSAGE_ACTION] включён)
 * @property workspaceInvite приглашение в воркспейс (если [UpdateSetting.WORKSPACE_INVITE] включён)
 */
@Serializable
data class UpdateV2(
    val updateId: Long,
    val message: Message? = null,
    val notification: Notification? = null,
    val messageAction: MessageAction? = null,
    val workspaceInvite: WorkspaceInvite? = null
)

/**
 * Уведомление о событии в воркспейсе (v2).
 *
 * Содержит одно из событий: изменение участников, чатов, реакций и т.д.
 */
@Serializable
data class Notification(
    val workspaceId: String,
    val timestamp: String,
    val memberJoinedEvent: MemberEvent? = null,
    val memberInvitedEvent: MemberEvent? = null,
    val memberLeftEvent: MemberEvent? = null,
    val memberChangedRoleEvent: MemberChangedRoleEvent? = null,
    val chatCreatedEvent: ChatCreatedNotification? = null,
    val reactionToggledEvent: ReactionToggledEvent? = null,
    val messageEditedEvent: MessageRefEvent? = null,
    val messageDeletedEvent: MessageRefEvent? = null,
    val chatMemberJoinedEvent: ChatMemberEvent? = null,
    val chatMemberLeftEvent: ChatMemberEvent? = null,
    val chatMemberInvitedEvent: ChatMemberInvitedEvent? = null,
    val chatMemberChangedRoleEvent: ChatMemberChangedRoleEvent? = null,
    val chatArchivedEvent: ChatRefEvent? = null,
    val chatUnarchivedEvent: ChatRefEvent? = null
)

/** Событие, связанное с участником воркспейса (v2). */
@Serializable
data class MemberEvent(val memberId: String)

/** Событие изменения роли участника в воркспейсе (v2). */
@Serializable
data class MemberChangedRoleEvent(
    val memberId: String,
    val newRole: WorkspaceRole
)

/** Уведомление о создании чата (v2). */
@Serializable
data class ChatCreatedNotification(
    val chatId: String,
    val metadata: ChatMetadata
)

/** Событие переключения реакции на сообщение (v2). */
@Serializable
data class ReactionToggledEvent(
    val chatId: String,
    val messageId: String,
    val emoji: String,
    val reactedBy: String,
    val wasSet: Boolean
)

/** Событие, связанное с сообщением (редактирование/удаление) (v2). */
@Serializable
data class MessageRefEvent(
    val chatId: String,
    val messageId: String
)

/** Событие, связанное с участником чата (v2). */
@Serializable
data class ChatMemberEvent(
    val chatId: String,
    val memberId: String
)

/** Событие приглашения участника в чат (v2). */
@Serializable
data class ChatMemberInvitedEvent(
    val chatId: String,
    val memberId: String,
    val inviterMemberId: String
)

/** Событие изменения роли участника в чате (v2). */
@Serializable
data class ChatMemberChangedRoleEvent(
    val chatId: String,
    val memberId: String,
    val newRole: ChatRole
)

/** Событие, связанное с чатом (архивация/разархивация) (v2). */
@Serializable
data class ChatRefEvent(val chatId: String)

/**
 * Действие с кнопкой сообщения (v2).
 *
 * Приходит при нажатии пользователем [CommandButton] в сообщении бота.
 */
@Serializable
data class MessageAction(
    val workspaceId: String,
    val chatId: String,
    val messageId: String,
    val pressedButtonCommand: CommandButton? = null
)

/** Приглашение бота в воркспейс (v2). */
@Serializable
data class WorkspaceInvite(
    val workspaceId: String,
    val inviterName: String,
    val inviterEmail: String
)
