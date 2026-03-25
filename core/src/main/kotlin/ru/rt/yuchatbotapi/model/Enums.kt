package ru.rt.yuchatbotapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Тип воркспейс-чата (v1). */
@Serializable
enum class WorkspaceChatType {
    /** Приватный чат (видим только участникам). */
    PRIVATE,
    /** Публичный чат (видим всем в воркспейсе). */
    PUBLIC,
    /** Общий чат воркспейса (создаётся автоматически). */
    GENERAL,
    /** Breakout-чат (для подгрупп). */
    BREAKOUT
}

/** Роль участника в чате (v1). */
@Serializable
enum class ChatMemberRoleType {
    CHAT_MEMBER, CHAT_ADMIN, CHAT_OWNER
}

/** Роль участника в чате (v2). */
@Serializable
enum class ChatRole {
    MEMBER, ADMIN, OWNER
}

/** Разрешения участника в чате (v2). */
@Serializable
enum class ChatPermission {
    CHANGE_ROLE, ARCHIVE_CHAT, RENAME_CHAT, UPDATE_CONFIG,
    KICK_FROM_CHAT, SEND_MESSAGES_TO_ANNOUNCE_CHANNEL,
    @SerialName("SEND_MESSAGE_TO_ANNOUNCE_CHANNEL")
    SEND_MESSAGE_TO_ANNOUNCE_CHANNEL,
    PIN_CHAT_MESSAGE, SCHEDULE_CONFERENCE, DELETE_CHAT_MESSAGE
}

/** Роль участника в воркспейсе (v2). */
@Serializable
enum class WorkspaceRole {
    MEMBER, ADMIN, OWNER, GUEST, GUEST_CALLER
}

/** Роль участника в воркспейсе (v1). */
@Serializable
enum class MemberRoleType {
    MEMBER, ADMIN, OWNER, GUEST, GUEST_CALLER
}

/** Статус участника в воркспейсе. */
@Serializable
enum class MemberStatus {
    /** Ожидает подтверждения. */
    PENDING,
    /** Активный участник. */
    ACTIVE,
    /** Заблокированный участник. */
    SUSPENDED
}

/** Тип аккаунта. */
@Serializable
enum class AccountType {
    REGULAR, BOT, VOICE_BOT, INTEGRATION_BOT, GUEST_ACCOUNT, GUEST
}

/** Местоположение пользователя (статус). */
@Serializable
enum class AccountLocation {
    LOCATION_NOT_SET, NOT_SET, OFFICE, HOME, VACATION
}

/** Тип медиафайла для загрузки (v1). */
@Serializable
enum class MediaType {
    RAW, IMAGE, AUDIO, VIDEO, PDF, DOC, XLS, PPT
}

/** Тип сообщения (v2). */
@Serializable
enum class MessageType {
    /** Пользовательское сообщение. */
    USER,
    /** Системное сообщение (события чата). */
    SYSTEM
}

/** Типы обновлений для настройки подписки (v2). */
@Serializable
enum class UpdateSetting {
    /** Новые сообщения. */
    MESSAGE,
    /** Действия с кнопками сообщений. */
    MESSAGE_ACTION,
    /** Уведомления (участники, чаты, реакции). */
    NOTIFICATION,
    /** Приглашения в воркспейс. */
    WORKSPACE_INVITE;

    companion object {
        /** Все типы обновлений. */
        val ALL = entries
    }
}

/** Тип событий для чата событий (v2). */
@Serializable
enum class EventsType {
    CALLS, MENTIONS
}

/** Область видимости бота (v2). */
@Serializable
enum class BotScopeType {
    /** Публичный бот. */
    @SerialName("pub") PUB,
    /** Бот уровня организации. */
    @SerialName("org") ORG,
    /** Бот уровня воркспейса. */
    @SerialName("ws") WS
}
