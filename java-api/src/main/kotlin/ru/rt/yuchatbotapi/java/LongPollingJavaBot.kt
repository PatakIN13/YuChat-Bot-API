package ru.rt.yuchatbotapi.java

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.model.UpdateSetting
import ru.rt.yuchatbotapi.model.UpdateV1
import ru.rt.yuchatbotapi.model.UpdateV2
import ru.rt.yuchatbotapi.polling.PollingOptions
import java.util.function.Consumer

/**
 * Java-совместимый long-polling бот.
 *
 * <pre>{@code
 * YuChatBotJavaClient client = new YuChatBotJavaClient("token", "https://yuchat.ai");
 * LongPollingJavaBot polling = new LongPollingJavaBot(client);
 *
 * polling.onMessage(update -> {
 *     var msg = update.getNewChatMessage();
 *     client.messages.send(msg.getWorkspaceId(), msg.getChatId(), "Echo: " + msg.getText()).join();
 * });
 *
 * polling.start();
 * }</pre>
 */
class LongPollingJavaBot @JvmOverloads constructor(
    private val client: YuChatBotJavaClient,
    private val options: PollingOptions = PollingOptions()
) {
    private val logger = LoggerFactory.getLogger(LongPollingJavaBot::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    // V1 handlers
    private var onUpdateV1: Consumer<UpdateV1>? = null
    private var onMessageV1: Consumer<UpdateV1>? = null
    private var onInviteV1: Consumer<UpdateV1>? = null
    private var onJoinV1: Consumer<UpdateV1>? = null
    private var onLeaveV1: Consumer<UpdateV1>? = null

    // V2 handlers
    private var onUpdateV2: Consumer<UpdateV2>? = null
    private var onMessageV2: Consumer<UpdateV2>? = null
    private var onNotificationV2: Consumer<UpdateV2>? = null
    private var onMessageActionV2: Consumer<UpdateV2>? = null
    private var onWorkspaceInviteV2: Consumer<UpdateV2>? = null

    private var onError: Consumer<Throwable>? = null

    // ── V1 handlers ──

    /** Обработчик любого обновления (v1) */
    fun onUpdate(handler: Consumer<UpdateV1>) { onUpdateV1 = handler }

    /** Обработчик нового сообщения (v1) */
    fun onMessage(handler: Consumer<UpdateV1>) { onMessageV1 = handler }

    /** Обработчик приглашения в чат (v1) */
    fun onInvite(handler: Consumer<UpdateV1>) { onInviteV1 = handler }

    /** Обработчик присоединения к чату (v1) */
    fun onJoin(handler: Consumer<UpdateV1>) { onJoinV1 = handler }

    /** Обработчик выхода из чата (v1) */
    fun onLeave(handler: Consumer<UpdateV1>) { onLeaveV1 = handler }

    // ── V2 handlers ──

    /** Обработчик любого обновления (v2) */
    fun onUpdateV2(handler: Consumer<UpdateV2>) { onUpdateV2 = handler }

    /** Обработчик сообщения (v2) */
    fun onMessageV2(handler: Consumer<UpdateV2>) { onMessageV2 = handler }

    /** Обработчик уведомления (v2) */
    fun onNotification(handler: Consumer<UpdateV2>) { onNotificationV2 = handler }

    /** Обработчик действия с сообщением (v2) */
    fun onMessageAction(handler: Consumer<UpdateV2>) { onMessageActionV2 = handler }

    /** Обработчик приглашения в воркспейс (v2) */
    fun onWorkspaceInvite(handler: Consumer<UpdateV2>) { onWorkspaceInviteV2 = handler }

    /** Обработчик ошибок */
    fun onError(handler: Consumer<Throwable>) { onError = handler }

    /** Запускает long-polling в фоновом потоке */
    fun start() {
        val kotlinClient = client.kotlinClient()

        job = scope.launch {
            if (options.apiVersion == 2 && options.autoConfigureV2) {
                try {
                    kotlinClient.updates.setUpdateSettings(
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
                    onError?.accept(e)
                    return@launch
                }
            }

            var offset: Long? = null

            while (isActive) {
                try {
                    if (options.apiVersion == 2) {
                        val updates = kotlinClient.updates.getUpdatesV2(offset, options.limit)
                        for (update in updates) {
                            dispatchV2(update)
                            offset = update.updateId + 1
                        }
                    } else {
                        val updates = kotlinClient.updates.getUpdates(offset, options.limit)
                        for (update in updates) {
                            dispatchV1(update)
                            offset = update.updateId + 1
                        }
                    }
                    delay(options.pollDelayMs)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Polling error", e)
                    onError?.accept(e)
                    delay(options.errorDelayMs)
                }
            }
        }
    }

    /** Останавливает polling */
    fun stop() {
        job?.cancel()
        job = null
    }

    /** Проверяет, запущен ли polling */
    fun isRunning(): Boolean = job?.isActive == true

    private fun dispatchV1(update: UpdateV1) {
        onUpdateV1?.accept(update)
        when {
            update.newChatMessage != null -> onMessageV1?.accept(update)
            update.inviteToChat != null -> onInviteV1?.accept(update)
            update.joinedToChat != null -> onJoinV1?.accept(update)
            update.leftFromChat != null -> onLeaveV1?.accept(update)
        }
    }

    private fun dispatchV2(update: UpdateV2) {
        onUpdateV2?.accept(update)
        when {
            update.message != null -> onMessageV2?.accept(update)
            update.notification != null -> onNotificationV2?.accept(update)
            update.messageAction != null -> onMessageActionV2?.accept(update)
            update.workspaceInvite != null -> onWorkspaceInviteV2?.accept(update)
        }
    }
}
