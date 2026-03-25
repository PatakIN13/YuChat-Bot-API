package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

/**
 * Информация о настроенном webhook (v1/v2).
 *
 * @property hasCustomCertificate `true` если используется самоподписанный сертификат
 * @property pendingUpdateCount количество необработанных обновлений в очереди
 */
@Serializable
data class WebhookInfo(
    val url: String,
    val hasCustomCertificate: Boolean,
    val pendingUpdateCount: Int? = null,
    val lastErrorDate: String? = null,
    val lastErrorMessage: String? = null,
    val updateTypes: List<UpdateSetting>? = null
)

/**
 * Запрос на установку webhook.
 *
 * @property url публичный HTTPS URL для приёма обновлений
 * @property certificate Base64-encoded PEM-сертификат (для самоподписанных)
 * @property secretToken секретный токен для валидации запросов (передаётся в заголовке `X-YuChat-Bot-Api-Secret-Token`)
 * @property updateTypes типы обновлений для получения (по умолчанию все)
 */
@Serializable
data class SetWebhookRequest(
    val url: String,
    val certificate: String? = null,
    val secretToken: String? = null,
    val updateTypes: List<UpdateSetting>? = null
)

/**
 * Информация о боте (v2).
 *
 * Возвращается методом [ru.rt.yuchatbotapi.api.BotApi.getMe].
 *
 * @property profile профиль аккаунта бота
 * @property workspaces список ID воркспейсов, в которых состоит бот
 * @property updateApiVersion текущая версия формата обновлений (1 или 2)
 * @property updateSettings включённые типы обновлений
 * @property autoAcceptWorkspaceInvite автоматически принимать приглашения в воркспейсы
 * @property scope область видимости бота
 */
@Serializable
data class MeInfo(
    val profile: Profile,
    val workspaces: List<String>,
    val updateApiVersion: Int,
    val updateSettings: List<UpdateSetting>,
    val autoAcceptWorkspaceInvite: Boolean,
    val scope: BotScope
)

/**
 * Область видимости бота (v2).
 *
 * @property type тип области: [BotScopeType.PUB] (публичный), [BotScopeType.ORG] (организация), [BotScopeType.WS] (воркспейс)
 * @property organizationId ID организации (для [BotScopeType.ORG])
 * @property workspaceId ID воркспейса (для [BotScopeType.WS])
 */
@Serializable
data class BotScope(
    val type: BotScopeType,
    val organizationId: String? = null,
    val workspaceId: String? = null
)

/** Ответ на запрос URL для загрузки файла (v2). */
@Serializable
data class FileUploadResponse(
    val fileId: String,
    val uploadUrl: String
)

/** Ответ на запрос URL для скачивания файла. */
@Serializable
data class FileDownloadResponse(
    val url: String
)

/** Ответ на запрос pre-signed URL для загрузки файла (v1). */
@Serializable
data class FilePreSignedResponse(
    val url: String,
    val fileId: String
)

// ── Response models (v2) ──

/** Ответ с списком сообщений (v2). */
@Serializable
data class GetMessagesResponse(
    val messages: List<Message>
)

/** Ответ на удаление сообщений (v2). */
@Serializable
data class DeleteMessagesResponse(
    val updatedAt: String,
    val deletedMessageIds: List<String>
)

/** Ответ на добавление/снятие реакции (v2). */
@Serializable
data class ToggleReactionResponse(
    val reactionAdded: Boolean
)

/**
 * Ответ со списком участников (v2, пагинация).
 *
 * @property membershipIds ID участников на текущей странице
 * @property prevPageToken токен предыдущей страницы
 * @property nextPageToken токен следующей страницы
 */
@Serializable
data class GetMembersResponse(
    val membershipIds: List<String>,
    val prevPageToken: String? = null,
    val nextPageToken: String? = null
)

/**
 * Ответ со списком чатов (v2, пагинация).
 *
 * @property chatIds ID чатов на текущей странице
 * @property prevPageToken токен предыдущей страницы
 * @property nextPageToken токен следующей страницы
 */
@Serializable
data class GetChatsResponse(
    val chatIds: List<String>,
    val prevPageToken: String? = null,
    val nextPageToken: String? = null
)
