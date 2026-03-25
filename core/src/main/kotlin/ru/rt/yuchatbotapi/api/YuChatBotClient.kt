package ru.rt.yuchatbotapi.api

import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import java.io.Closeable

/**
 * Главный клиент YuChat Bot API.
 *
 * Пример использования:
 * ```kotlin
 * val bot = YuChatBotClient("your-jwt-token")
 *
 * // Отправка сообщения (v1)
 * bot.messages.send(workspaceId = "ws-1", chatId = "chat-1", text = "Привет!")
 *
 * // Закрепление (v2)
 * bot.messages.pin(chatId = "chat-1", messageId = "msg-1")
 *
 * // Информация о боте (v2)
 * val me = bot.bot.getMe()
 * ```
 */
class YuChatBotClient(
    token: String,
    configure: ClientConfig.() -> Unit = {}
) : Closeable {

    private val config = ClientConfig().apply(configure)
    private val httpClient = YuChatHttpClient(
        token = token,
        baseUrl = config.baseUrl,
        maxRetries = config.maxRetries,
        retryDelayMs = config.retryDelayMs,
        connectTimeoutMs = config.connectTimeoutMs,
        requestTimeoutMs = config.requestTimeoutMs,
        socketTimeoutMs = config.socketTimeoutMs
    )

    /** Работа с сообщениями */
    val messages = MessagesApi(httpClient)

    /** Работа с чатами */
    val chats = ChatsApi(httpClient)

    /** Работа с участниками воркспейса */
    val members = MembersApi(httpClient)

    /** Работа с файлами */
    val files = FilesApi(httpClient)

    /** Получение обновлений */
    val updates = UpdatesApi(httpClient)

    /** Управление вебхуками */
    val webhooks = WebhooksApi(httpClient)

    /** Информация о боте */
    val bot = BotApi(httpClient)

    override fun close() {
        httpClient.close()
    }
}

/** Конфигурация клиента */
class ClientConfig {
    /** Базовый URL API */
    var baseUrl: String = "https://yuchat.ai"

    /** Максимальное количество повторных попыток при 429 */
    var maxRetries: Int = 3

    /** Начальная задержка перед повторной попыткой (мс) */
    var retryDelayMs: Long = 1000L

    /** Таймаут подключения (мс) */
    var connectTimeoutMs: Long = 10_000L

    /** Таймаут ожидания ответа (мс). Для long-polling рекомендуется 60000+ */
    var requestTimeoutMs: Long = 60_000L

    /** Таймаут на чтение сокета (мс) */
    var socketTimeoutMs: Long = 60_000L
}
