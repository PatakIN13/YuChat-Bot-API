package ru.rt.yuchatbotapi.api

import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*

/**
 * Управление вебхуками.
 *
 * [setWebhook], [deleteWebhook], [getWebhookInfo] → API v1.
 * [setWebhookV2], [deleteWebhookV2], [getWebhookInfoV2] → API v2.
 */
class WebhooksApi internal constructor(private val client: YuChatHttpClient) {

    /** Установка вебхука (v1) */
    suspend fun setWebhook(request: SetWebhookRequest) {
        client.postNoContent("/public/v1/bot.setWebhook", request)
    }

    /** Удаление вебхука (v1) */
    suspend fun deleteWebhook() {
        client.deleteNoContent("/public/v1/bot.deleteWebhook")
    }

    /** Информация о вебхуке (v1) */
    suspend fun getWebhookInfo(): WebhookInfo {
        @kotlinx.serialization.Serializable
        data class Resp(val webhookInfo: WebhookInfo)
        return client.get<Resp>("/public/v1/bot.getWebhookInfo").webhookInfo
    }

    // ── v2 варианты ──

    /** Установка вебхука (v2) */
    suspend fun setWebhookV2(request: SetWebhookRequest) {
        client.postNoContent("/public/v2/setWebhook", request)
    }

    /** Удаление вебхука (v2) */
    suspend fun deleteWebhookV2() {
        client.deleteNoContent("/public/v2/deleteWebhook")
    }

    /** Информация о вебхуке (v2) */
    suspend fun getWebhookInfoV2(): WebhookInfo {
        return client.get("/public/v2/getWebhookInfo")
    }
}
