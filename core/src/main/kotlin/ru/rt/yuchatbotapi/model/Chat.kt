package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

// ── v1 модели чатов ──

/**
 * Чат воркспейса (v1).
 *
 * Возвращается методами [ru.rt.yuchatbotapi.api.ChatsApi.invite] и [ru.rt.yuchatbotapi.api.ChatsApi.kick].
 */
@Serializable
data class WorkspaceChat(
    @get:JvmName("getChatId") val chatId: ChatId? = null,
    @get:JvmName("getWorkspaceId") val workspaceId: WorkspaceId? = null,
    val name: String? = null,
    val type: WorkspaceChatType? = null,
    val announceChannel: Boolean? = null,
    val description: String? = null,
    @get:JvmName("getMembershipIds") val membershipIds: List<MembershipId>? = null
)

/**
 * Чат с метаинформацией о членстве (v1).
 *
 * Возвращается в [ru.rt.yuchatbotapi.api.ListWorkspaceChatsResponse].
 */
@Serializable
data class WorkspaceChatMembership(
    val chat: WorkspaceChat? = null,
    val role: ChatMemberRoleType? = null,
    val permissions: List<ChatPermission>? = null
)

// ── v2 модели чатов ──

/**
 * Информация о чате с ролью и разрешениями текущего пользователя (v2).
 *
 * @property chatRole роль бота в данном чате
 * @property chatPermissions разрешения бота в чате
 * @property archivedAt время архивации (если чат архивирован)
 */
@Serializable
data class ChatMembership(
    @get:JvmName("getChatId") val chatId: ChatId,
    @get:JvmName("getWorkspaceId") val workspaceId: WorkspaceId,
    val metadata: ChatMetadata,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String? = null,
    val archivedBy: String? = null,
    val chatRole: ChatRole? = null,
    val chatPermissions: List<ChatPermission>? = null
)

/**
 * Метаданные чата (v2).
 *
 * Ровно одно из полей заполнено в зависимости от типа чата.
 */
@Serializable
data class ChatMetadata(
    val personal: PersonalChatMetadata? = null,
    val workspace: WorkspaceChatMetadata? = null,
    val thread: ThreadChatMetadata? = null,
    val conference: ConferenceChatMetadata? = null,
    val userEvents: UserEventsChatMetadata? = null
)

/** Метаданные личного чата (v2). */
@Serializable
data class PersonalChatMetadata(
    @get:JvmName("getOtherMembershipId") val otherMembershipId: MembershipId
)

/**
 * Метаданные воркспейс-чата (v2).
 *
 * @property announce `true` если чат является каналом объявлений
 * @property autoJoinNewMembers `true` если новые участники воркспейса автоматически добавляются
 */
@Serializable
data class WorkspaceChatMetadata(
    val name: String,
    val announce: Boolean,
    val chatType: WorkspaceChatType,
    val autoJoinNewMembers: Boolean,
    val memberCount: Int,
    val description: String? = null
)

/** Метаданные тред-чата (v2). */
@Serializable
data class ThreadChatMetadata(
    @get:JvmName("getParentChatId") val parentChatId: ChatId,
    @get:JvmName("getParentMessageId") val parentMessageId: ChatMessageId
)

/** Метаданные чата конференции (v2). */
@Serializable
data class ConferenceChatMetadata(
    val conferenceId: String
)

/** Метаданные чата пользовательских событий (v2). */
@Serializable
data class UserEventsChatMetadata(
    @get:JvmName("getOwner") val owner: MembershipId
)
