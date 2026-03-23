package ru.rt.yuchatbotapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── v1 модели сообщений ──

/**
 * Новое сообщение в чате (v1).
 *
 * Приходит в [UpdateV1.newChatMessage] при получении обновлений через long-polling или webhook.
 *
 * @property workspaceId ID воркспейса
 * @property chatId ID чата
 * @property messageId уникальный ID сообщения
 * @property author ID аккаунта автора
 * @property text текст сообщения (поддерживает Markdown-разметку)
 * @property parentMessageId ID родительского сообщения (для ответов)
 * @property parentMessageAuthor ID автора родительского сообщения
 * @property fileIds список ID прикреплённых файлов
 * @property createdAt время создания (ISO 8601)
 */
@Serializable
data class NewChatMessage(
    val workspaceId: WorkspaceId,
    val chatId: ChatId,
    val messageId: ChatMessageId,
    val author: AccountId,
    @SerialName("markdown") val text: String,
    val parentMessageId: ChatMessageId? = null,
    val parentMessageAuthor: AccountId? = null,
    val fileIds: List<String>? = null,
    val createdAt: String
)

/**
 * Приглашение бота в чат (v1).
 *
 * Приходит в [UpdateV1.inviteToChat].
 */
@Serializable
data class InviteToChat(
    val workspaceId: WorkspaceId,
    val chatId: ChatId,
    val inviter: AccountId
)

/** Событие присоединения участников к чату (v1). */
@Serializable
data class JoinedToChat(
    val workspaceId: WorkspaceId,
    val chatId: ChatId,
    val inviter: MembershipId? = null,
    val joined: List<MembershipId>
)

/** Событие выхода участников из чата (v1). */
@Serializable
data class LeftFromChat(
    val workspaceId: WorkspaceId,
    val chatId: ChatId,
    val kicker: MembershipId? = null,
    val left: List<MembershipId>
)

// ── v2 модели сообщений ──

/**
 * Сообщение (v2).
 *
 * Расширенная модель сообщения с реакциями, тредами и закреплением.
 *
 * @property messageType тип сообщения ([MessageType.USER] или [MessageType.SYSTEM])
 * @property membershipId ID участника-автора в контексте воркспейса
 * @property content содержимое сообщения (текст, файлы, системные события)
 * @property reactions список реакций на сообщение
 * @property threadId ID треда (если сообщение в треде)
 * @property pinnedAt время закрепления (если закреплено)
 */
@Serializable
data class Message(
    val workspaceId: String,
    val chatId: String,
    val messageId: String,
    val messageType: MessageType,
    val membershipId: String,
    val createdAt: String,
    val updatedAt: String,
    val content: MessageContent,
    val inReplyToMessageId: String? = null,
    val reactions: List<Reaction>? = null,
    val threadId: String? = null,
    val pinnedAt: String? = null
)

/**
 * Содержимое сообщения (v2).
 *
 * Только одно из полей заполнено в зависимости от типа сообщения.
 */
@Serializable
data class MessageContent(
    val text: String? = null,
    val fileIds: List<String>? = null,
    val conferenceId: String? = null,
    val systemEvent: SystemEvent? = null,
    val forwardedContent: ForwardedContent? = null,
    val userEvent: UserEvent? = null
)

/** Реакция на сообщение (v2). */
@Serializable
data class Reaction(
    val emoji: String,
    val count: Int
)

/** Пересланный контент (v2). */
@Serializable
data class ForwardedContent(
    val sourceWorkspaceId: String,
    val sourceChatId: String,
    val forwardedMessages: List<Message>
)

/** Системное событие в сообщении (v2). Содержит одно из событий чата. */
@Serializable
data class SystemEvent(
    val chatCreated: ChatCreatedEvent? = null,
    val chatConvertedToPrivate: ChatConvertedEvent? = null,
    val membersJoined: MembersJoinedEvent? = null,
    val membersLeft: MembersLeftEvent? = null,
    val chatRenamed: ChatRenamedEvent? = null
)

/** Событие создания чата (v2). */
@Serializable
data class ChatCreatedEvent(
    val creatorMembershipId: String,
    val membershipIds: List<String>? = null
)

/** Событие конвертации чата в приватный (v2). */
@Serializable
data class ChatConvertedEvent(
    val initiatorMembershipId: String
)

/** Событие присоединения участников (v2). */
@Serializable
data class MembersJoinedEvent(
    val inviterMembershipId: String? = null,
    val membershipIds: List<String>
)

/** Событие выхода участников (v2). */
@Serializable
data class MembersLeftEvent(
    val kickerMembershipId: String? = null,
    val membershipIds: List<String>
)

/** Событие переименования чата (v2). */
@Serializable
data class ChatRenamedEvent(
    val initiatorMembershipId: String,
    val newName: String
)

/** Пользовательское событие (конференции, упоминания) (v2). */
@Serializable
data class UserEvent(
    val conferenceMissed: ConferenceEvent? = null,
    val conferenceDeclined: ConferenceEvent? = null,
    val conferenceDirectInvite: ConferenceEvent? = null,
    val conferenceAnnounce: ConferenceEvent? = null,
    val chatMessageMention: ChatMessageMention? = null
)

/** Событие конференции (v2). */
@Serializable
data class ConferenceEvent(
    val conferenceId: String,
    val conferenceTarget: ConferenceTarget,
    val startedAt: String,
    val inviterMemberId: String? = null
)

/** Цель конференции (v2). */
@Serializable
data class ConferenceTarget(
    val channelCall: ChannelCall? = null,
    val watercooler: Watercooler? = null
)

/** Звонок в канал (v2). */
@Serializable
data class ChannelCall(val chatId: String)

/** Watercooler-конференция (v2). */
@Serializable
data class Watercooler(
    val scope: String,
    val membershipIds: List<String>
)

/** Упоминание в сообщении чата (v2). */
@Serializable
data class ChatMessageMention(
    val chatId: String,
    val chatMessage: Message
)
