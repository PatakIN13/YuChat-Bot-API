package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

/** ID воркспейса */
@JvmInline
@Serializable
value class WorkspaceId(val value: String) {
    override fun toString(): String = value
}

/** ID чата */
@JvmInline
@Serializable
value class ChatId(val value: String) {
    override fun toString(): String = value
}

/** ID сообщения */
@JvmInline
@Serializable
value class ChatMessageId(val value: String) {
    override fun toString(): String = value
}

/** ID аккаунта пользователя */
@JvmInline
@Serializable
value class AccountId(val value: String) {
    override fun toString(): String = value
}

/** ID пользователя в конкретном воркспейсе */
@JvmInline
@Serializable
value class MembershipId(val value: String) {
    override fun toString(): String = value
}
