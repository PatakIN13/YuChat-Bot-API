package ru.rt.yuchatbotapi.api

import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.MeInfo

/**
 * Информация о боте (API v2).
 */
class BotApi internal constructor(private val client: YuChatHttpClient) {

    /**
     * Получение информации о боте (v2).
     *
     * @return [MeInfo] с профилем, воркспейсами, настройками и областью видимости
     */
    suspend fun getMe(): MeInfo {
        return client.post("/public/v2/getMe", null)
    }
}
