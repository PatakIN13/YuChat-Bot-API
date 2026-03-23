package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

// ── v1 модели участников ──

/**
 * Участник воркспейса (v1).
 *
 * Возвращается методом [ru.rt.yuchatbotapi.api.MembersApi.list].
 */
@Serializable
data class Member(
    val memberId: String? = null,
    val profile: ProfileV1? = null,
    val roleType: MemberRoleType? = null,
    val presence: PresenceV1? = null,
    val status: MemberStatus? = null
)

/**
 * Профиль пользователя (v1).
 *
 * В v1 используется `profileId` + `primaryEmail` (в отличие от v2 [Profile]).
 */
@Serializable
data class ProfileV1(
    val profileId: String? = null,
    val primaryEmail: String? = null,
    val fullName: String? = null,
    val type: AccountType? = null,
    val details: AccountDetails? = null
)

/** Статус присутствия пользователя (v1). */
@Serializable
data class PresenceV1(
    val isOnline: Boolean? = null,
    val isOnCall: Boolean? = null,
    val lastSeenAt: String? = null
)

// ── v2 модели участников ──

/**
 * Информация об участнике воркспейса (v2).
 *
 * Расширенная модель с временными метками, часовым поясом и ролью.
 *
 * @property membershipId ID участника в контексте воркспейса
 * @property workspaceRole роль в воркспейсе (MEMBER, ADMIN, OWNER, ...)
 * @property timeZone часовой пояс пользователя (IANA, например "Europe/Moscow")
 */
@Serializable
data class MemberInfo(
    val membershipId: String,
    val profile: Profile,
    val createdAt: String,
    val updatedAt: String,
    val presence: Presence,
    val memberStatus: MemberStatus,
    val joinedAt: String? = null,
    val workspaceRole: WorkspaceRole? = null,
    val timeZone: String? = null,
    val leftAt: String? = null
)

/**
 * Профиль пользователя (v2).
 *
 * В v2 используется `accountId` + `email` (в отличие от v1 [ProfileV1]).
 */
@Serializable
data class Profile(
    val accountId: String,
    val createdAt: String,
    val updatedAt: String,
    val accountType: AccountType,
    val email: String? = null,
    val fullName: String? = null,
    val accountDetails: AccountDetails? = null
)

/** Дополнительные сведения об аккаунте (должность, отдел, телефон). */
@Serializable
data class AccountDetails(
    val position: String? = null,
    val department: String? = null,
    val phoneNumber: String? = null,
    val location: AccountLocation? = null,
    val bio: String? = null,
    val status: String? = null
)

/** Статус присутствия пользователя (v2). */
@Serializable
data class Presence(
    val online: Boolean,
    val onCall: Boolean,
    val since: String
)
