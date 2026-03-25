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
    @get:JvmName("getWorkspaceId") val workspaceId: WorkspaceId,
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
data class MemberEvent(@get:JvmName("getMemberId") val memberId: MembershipId)

/** Событие изменения роли участника в воркспейсе (v2). */
@Serializable
data class MemberChangedRoleEvent(
    @get:JvmName("getMemberId") val memberId: MembershipId,
    val newRole: WorkspaceRole
)

/** Уведомление о создании чата (v2). */
@Serializable
data class ChatCreatedNotification(
    @get:JvmName("getChatId") val chatId: ChatId,
    val metadata: ChatMetadata
)

/** Событие переключения реакции на сообщение (v2). */
@Serializable
data class ReactionToggledEvent(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMessageId") val messageId: ChatMessageId,
    val emoji: String,
    @get:JvmName("getReactedBy") val reactedBy: MembershipId,
    val wasSet: Boolean
)

/** Событие, связанное с сообщением (редактирование/удаление) (v2). */
@Serializable
data class MessageRefEvent(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMessageId") val messageId: ChatMessageId
)

/** Событие, связанное с участником чата (v2). */
@Serializable
data class ChatMemberEvent(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMemberId") val memberId: MembershipId
)

/** Событие приглашения участника в чат (v2). */
@Serializable
data class ChatMemberInvitedEvent(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMemberId") val memberId: MembershipId,
    @get:JvmName("getInviterMemberId") val inviterMemberId: MembershipId
)

/** Событие изменения роли участника в чате (v2). */
@Serializable
data class ChatMemberChangedRoleEvent(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMemberId") val memberId: MembershipId,
    val newRole: ChatRole
)

/** Событие, связанное с чатом (архивация/разархивация) (v2). */
@Serializable
data class ChatRefEvent(@get:JvmName("getChatId") val chatId: ChatId)

/**
 * Действие с кнопкой сообщения (v2).
 *
 * Приходит при нажатии пользователем [CommandButton] в сообщении бота.
 */
@Serializable
data class MessageAction(
    @get:JvmName("getWorkspaceId") val workspaceId: WorkspaceId,
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getMessageId") val messageId: ChatMessageId,
    val pressedButtonCommand: CommandButton? = null
)

/** Приглашение бота в воркспейс (v2). */
@Serializable
data class WorkspaceInvite(
    @get:JvmName("getWorkspaceId") val workspaceId: WorkspaceId,
    val inviterName: String,
    val inviterEmail: String
)
