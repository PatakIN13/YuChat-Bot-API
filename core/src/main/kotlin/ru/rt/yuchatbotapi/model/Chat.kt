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
    val chatId: String? = null,
    val workspaceId: String? = null,
    val name: String? = null,
    val type: WorkspaceChatType? = null,
    val announceChannel: Boolean? = null,
    val description: String? = null,
    val membershipIds: List<String>? = null
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
    val chatId: String,
    val workspaceId: String,
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
    val otherMembershipId: String
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
    val parentChatId: String,
    val parentMessageId: String
)

/** Метаданные чата конференции (v2). */
@Serializable
data class ConferenceChatMetadata(
    val conferenceId: String
)

/** Метаданные чата пользовательских событий (v2). */
@Serializable
data class UserEventsChatMetadata(
    val owner: String
)
