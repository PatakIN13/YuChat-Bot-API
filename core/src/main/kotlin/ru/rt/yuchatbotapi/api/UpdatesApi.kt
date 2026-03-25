package ru.rt.yuchatbotapi.api

import kotlinx.serialization.Serializable
import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*

/**
 * Получение обновлений (long-polling).
 *
 * [getUpdates] → API v1 (по умолчанию).
 * [getUpdatesV2] → API v2 (требует предварительного вызова [setUpdateSettings]).
 * [getWorkspaceInvites], [acceptWorkspaceInvite], [rejectAllWorkspaceInvites] → API v2.
 */
class UpdatesApi internal constructor(private val client: YuChatHttpClient) {

    companion object {
        /** Таймаут для long-polling запросов (90 секунд) */
        private const val LONG_POLL_TIMEOUT_MS = 90_000L
    }

    /**
     * Получение обновлений (v1).
     *
     * @param offset ID обновления, начиная с которого получать (exclusive)
     * @param limit максимальное количество обновлений (1–100)
     * @return список [UpdateV1]
     */
    suspend fun getUpdates(
        offset: Long? = null,
        limit: Int? = null
    ): List<UpdateV1> {
        return client.get<List<UpdateV1>>("/public/v1/bot.getUpdates", mapOf(
            "offset" to offset,
            "limit" to limit
        ), requestTimeoutOverrideMs = LONG_POLL_TIMEOUT_MS)
    }

    /**
     * Получение обновлений (v2).
     *
     * Требует предварительного вызова [setUpdateSettings] с `updateApiVersion = 2`.
     *
     * @param offset ID обновления, начиная с которого получать (exclusive)
     * @param limit максимальное количество обновлений
     * @return список [UpdateV2]
     */
    suspend fun getUpdatesV2(
        offset: Long? = null,
        limit: Int? = null
    ): List<UpdateV2> {
        @Serializable
        data class Req(val offset: Long? = null, val limit: Int? = null)
        @Serializable
        data class Resp(val updates: List<UpdateV2>)
        return client.post<Resp>("/public/v2/getUpdates", Req(offset, limit),
            requestTimeoutOverrideMs = LONG_POLL_TIMEOUT_MS).updates
    }

    /**
     * Настройка типов обновлений и версии API (v2).
     *
     * Необходимо вызвать перед [getUpdatesV2] для переключения на формат v2.
     *
     * @param updateSettings список типов обновлений для получения
     * @param updateApiVersion версия API обновлений (1 или 2)
     * @param autoAcceptWorkspaceInvites автоматически принимать приглашения
     */
    suspend fun setUpdateSettings(
        updateSettings: List<UpdateSetting>,
        updateApiVersion: Int,
        autoAcceptWorkspaceInvites: Boolean = false
    ) {
        @Serializable
        data class Req(
            val updateSettings: List<UpdateSetting>,
            val updateApiVersion: Int,
            val autoAcceptWorkspaceInvites: Boolean
        )
        client.postNoContent("/public/v2/setUpdateSettings", Req(
            updateSettings, updateApiVersion, autoAcceptWorkspaceInvites
        ))
    }

    /** Получение приглашений в воркспейсы (v2) */
    suspend fun getWorkspaceInvites(): List<WorkspaceInvite> {
        @Serializable
        data class Resp(val workspaceInvites: List<WorkspaceInvite>)
        return client.post<Resp>("/public/v2/getMyWorkspaceInvites", null).workspaceInvites
    }

    /** Принятие приглашения в воркспейс (v2) */
    suspend fun acceptWorkspaceInvite(workspaceId: WorkspaceId) {
        @Serializable
        data class Req(val workspaceId: String)
        client.postNoContent("/public/v2/acceptWorkspaceInvite", Req(workspaceId.value))
    }

    /** Отклонение всех приглашений (v2) */
    suspend fun rejectAllWorkspaceInvites() {
        client.postNoContent("/public/v2/rejectAllWorkspaceInvites", null)
    }
}
