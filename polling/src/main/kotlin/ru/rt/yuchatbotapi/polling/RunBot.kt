package ru.rt.yuchatbotapi.polling

import ru.rt.yuchatbotapi.api.BotConfig
import ru.rt.yuchatbotapi.api.ClientConfig
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.handler.UpdateDispatcher

/**
 * Запускает polling-бота с минимальным бойлерплейтом.
 *
 * Автоматически загружает конфигурацию из `bot.properties` / env,
 * создаёт клиент, запускает polling и блокирует main thread.
 * Graceful shutdown при SIGTERM/SIGINT.
 *
 * Пример:
 * ```kotlin
 * fun main() = runPollingBot { client ->
 *     onMessage { msg ->
 *         msg.reply(client, "Echo: ${msg.text}")
 *     }
 * }
 * ```
 *
 * @param options настройки polling
 * @param clientConfig дополнительная конфигурация клиента
 * @param handlers DSL для регистрации обработчиков (client передаётся как параметр)
 */
fun runPollingBot(
    options: PollingOptions = PollingOptions(),
    clientConfig: ClientConfig.() -> Unit = {},
    handlers: UpdateDispatcher.(client: YuChatBotClient) -> Unit
) {
    val client = BotConfig.createClient(clientConfig)
    val polling = client.startPolling(options) { handlers(client) }

    Runtime.getRuntime().addShutdownHook(Thread {
        polling.stop()
        client.close()
    })

    Thread.currentThread().join()
}
