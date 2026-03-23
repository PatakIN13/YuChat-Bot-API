package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.YuChatBotClient
import java.io.Closeable

/**
 * Java-совместимый клиент YuChat Bot API.
 *
 * Все методы возвращают {@link java.util.concurrent.CompletableFuture} вместо suspend.
 *
 * <pre>{@code
 * YuChatBotJavaClient bot = new YuChatBotJavaClient("token");
 * bot.messages().send("ws-1", "chat-1", "Привет!")
 *     .thenAccept(msg -> System.out.println(msg.getChatMessageId()));
 * }</pre>
 */
class YuChatBotJavaClient @JvmOverloads constructor(
    token: String,
    baseUrl: String = "https://yuchat.ai",
    maxRetries: Int = 3,
    retryDelayMs: Long = 1000L
) : Closeable {

    private val delegate = YuChatBotClient(token) {
        this.baseUrl = baseUrl
        this.maxRetries = maxRetries
        this.retryDelayMs = retryDelayMs
    }

    /** Работа с сообщениями */
    @JvmField val messages = MessagesJavaApi(delegate.messages)

    /** Работа с чатами */
    @JvmField val chats = ChatsJavaApi(delegate.chats)

    /** Работа с участниками */
    @JvmField val members = MembersJavaApi(delegate.members)

    /** Работа с файлами */
    @JvmField val files = FilesJavaApi(delegate.files)

    /** Получение обновлений */
    @JvmField val updates = UpdatesJavaApi(delegate.updates)

    /** Управление вебхуками */
    @JvmField val webhooks = WebhooksJavaApi(delegate.webhooks)

    /** Информация о боте */
    @JvmField val bot = BotJavaApi(delegate.bot)

    /** Прямой доступ к Kotlin-клиенту (для продвинутых сценариев) */
    fun kotlinClient(): YuChatBotClient = delegate

    override fun close() {
        delegate.close()
    }
}
