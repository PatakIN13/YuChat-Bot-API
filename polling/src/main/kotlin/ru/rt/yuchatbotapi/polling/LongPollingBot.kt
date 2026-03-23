package ru.rt.yuchatbotapi.polling

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.model.UpdateSetting

/**
 * Long-polling бот.
 *
 * Пример:
 * ```kotlin
 * val bot = YuChatBotClient("token")
 * val polling = LongPollingBot(bot)
 *
 * polling.start {
 *     onMessage { update ->
 *         val msg = update.newChatMessage!!
 *         bot.messages.send(msg.workspaceId, msg.chatId, "Echo: ${msg.markdown}")
 *     }
 * }
 * ```
 */
class LongPollingBot(
    private val client: YuChatBotClient,
    private val options: PollingOptions = PollingOptions()
) {
    private val logger = LoggerFactory.getLogger(LongPollingBot::class.java)
    private var job: Job? = null

    /**
     * Запускает long-polling.
     * @param configure DSL для регистрации обработчиков обновлений
     */
    fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        configure: UpdateDispatcher.() -> Unit
    ): Job {
        val dispatcher = UpdateDispatcher().apply(configure)

        job = scope.launch {
            if (options.apiVersion == 2 && options.autoConfigureV2) {
                try {
                    client.updates.setUpdateSettings(
                        updateSettings = listOf(
                            UpdateSetting.MESSAGE,
                            UpdateSetting.NOTIFICATION,
                            UpdateSetting.MESSAGE_ACTION,
                            UpdateSetting.WORKSPACE_INVITE
                        ),
                        updateApiVersion = 2
                    )
                    logger.info("Switched to v2 update format")
                } catch (e: Exception) {
                    logger.error("Failed to configure v2 updates", e)
                    dispatcher.onError?.invoke(e)
                    return@launch
                }
            }

            var offset: Long? = null

            while (isActive) {
                try {
                    if (options.apiVersion == 2) {
                        val updates = client.updates.getUpdatesV2(offset, options.limit)
                        for (update in updates) {
                            dispatcher.dispatchV2(update)
                            offset = update.updateId + 1
                        }
                    } else {
                        val updates = client.updates.getUpdates(offset, options.limit)
                        for (update in updates) {
                            dispatcher.dispatchV1(update)
                            offset = update.updateId + 1
                        }
                    }
                    delay(options.pollDelayMs)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Polling error", e)
                    dispatcher.onError?.invoke(e)
                    delay(options.errorDelayMs)
                }
            }
        }

        return job!!
    }

    /** Останавливает polling */
    fun stop() {
        job?.cancel()
        job = null
    }
}

/**
 * Extension для удобного запуска polling из YuChatBotClient.
 */
fun YuChatBotClient.startPolling(
    options: PollingOptions = PollingOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    configure: UpdateDispatcher.() -> Unit
): LongPollingBot {
    val bot = LongPollingBot(this, options)
    bot.start(scope, configure)
    return bot
}
