package ru.rt.yuchatbotapi.java

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.model.*
import ru.rt.yuchatbotapi.polling.PollingOptions
import java.util.function.BiConsumer
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
    private var onMessageV1: Consumer<NewChatMessage>? = null
    private var onInviteV1: Consumer<InviteToChat>? = null
    private var onJoinV1: Consumer<JoinedToChat>? = null
    private var onLeaveV1: Consumer<LeftFromChat>? = null

    // V2 handlers
    private var onUpdateV2: Consumer<UpdateV2>? = null
    private var onMessageV2: Consumer<Message>? = null
    private var onNotificationV2: Consumer<Notification>? = null
    private var onMessageActionV2: Consumer<MessageAction>? = null
    private var onWorkspaceInviteV2: Consumer<WorkspaceInvite>? = null

    // Command handlers
    private val commandsV1 = mutableMapOf<String, BiConsumer<NewChatMessage, List<String>>>()
    private val commandsV2 = mutableMapOf<String, BiConsumer<Message, List<String>>>()

    private var onError: Consumer<Throwable>? = null

    // ── V1 handlers ──

    /** Обработчик любого обновления (v1) */
    fun onUpdate(handler: Consumer<UpdateV1>) { onUpdateV1 = handler }

    /** Обработчик нового сообщения (v1) */
    fun onMessage(handler: Consumer<NewChatMessage>) { onMessageV1 = handler }

    /** Обработчик приглашения в чат (v1) */
    fun onInvite(handler: Consumer<InviteToChat>) { onInviteV1 = handler }

    /** Обработчик присоединения к чату (v1) */
    fun onJoin(handler: Consumer<JoinedToChat>) { onJoinV1 = handler }

    /** Обработчик выхода из чата (v1) */
    fun onLeave(handler: Consumer<LeftFromChat>) { onLeaveV1 = handler }

    /** Обработчик команды (v1) */
    fun onCommand(command: String, handler: BiConsumer<NewChatMessage, List<String>>) {
        commandsV1[command] = handler
    }

    // ── V2 handlers ──

    /** Обработчик любого обновления (v2) */
    fun onUpdateV2(handler: Consumer<UpdateV2>) { onUpdateV2 = handler }

    /** Обработчик сообщения (v2) */
    fun onMessageV2(handler: Consumer<Message>) { onMessageV2 = handler }

    /** Обработчик уведомления (v2) */
    fun onNotification(handler: Consumer<Notification>) { onNotificationV2 = handler }

    /** Обработчик действия с сообщением (v2) */
    fun onMessageAction(handler: Consumer<MessageAction>) { onMessageActionV2 = handler }

    /** Обработчик приглашения в воркспейс (v2) */
    fun onWorkspaceInvite(handler: Consumer<WorkspaceInvite>) { onWorkspaceInviteV2 = handler }

    /** Обработчик команды (v2) */
    fun onCommandV2(command: String, handler: BiConsumer<Message, List<String>>) {
        commandsV2[command] = handler
    }

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

            // Фильтрация собственных сообщений бота
            var resolvedBotAccountId: AccountId? = null
            if (options.autoResolveBotId) {
                try {
                    val me = kotlinClient.bot.getMe()
                    resolvedBotAccountId = me.profile.accountId
                    logger.info("Self-message filtering enabled via getMe() (accountId={})", me.profile.accountId)
                } catch (e: Exception) {
                    logger.warn("autoResolveBotId: getMe() failed (v2 API unavailable?), falling back to manual botAccountId", e)
                    resolvedBotAccountId = options.botAccountId
                    if (resolvedBotAccountId != null) {
                        logger.info("Self-message filtering enabled via manual botAccountId (accountId={})", resolvedBotAccountId)
                    }
                }
            } else {
                resolvedBotAccountId = options.botAccountId
                if (resolvedBotAccountId != null) {
                    logger.info("Self-message filtering enabled (accountId={})", resolvedBotAccountId)
                }
            }
            val botAccountId = resolvedBotAccountId

            var offset: Long? = null

            // Пропускаем накопившиеся обновления при старте
            if (options.skipPending) {
                try {
                    var skipped = 0
                    while (true) {
                        if (options.apiVersion == 2) {
                            val pending = kotlinClient.updates.getUpdatesV2(offset, options.limit)
                            if (pending.isEmpty()) break
                            offset = pending.last().updateId + 1
                            skipped += pending.size
                        } else {
                            val pending = kotlinClient.updates.getUpdates(offset, options.limit)
                            if (pending.isEmpty()) break
                            offset = pending.last().updateId + 1
                            skipped += pending.size
                        }
                    }
                    if (skipped > 0) {
                        logger.info("Skipped {} pending updates", skipped)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to skip pending updates, starting from beginning", e)
                }
            }

            while (isActive) {
                try {
                    if (options.apiVersion == 2) {
                        val updates = kotlinClient.updates.getUpdatesV2(offset, options.limit)
                        for (update in updates) {
                            dispatchV2(update, botAccountId)
                            offset = update.updateId + 1
                        }
                    } else {
                        val updates = kotlinClient.updates.getUpdates(offset, options.limit)
                        for (update in updates) {
                            dispatchV1(update, botAccountId)
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

    private fun dispatchV1(update: UpdateV1, botAccountId: AccountId?) {
        onUpdateV1?.accept(update)
        update.newChatMessage?.let { msg ->
            if (botAccountId != null && msg.author == botAccountId) {
                logger.debug("Ignoring self-message (v1): {}", msg.messageId)
                return@let
            }
            if (!tryDispatchCommand(msg.text, msg, commandsV1)) {
                onMessageV1?.accept(msg)
            }
        }
        update.inviteToChat?.let { onInviteV1?.accept(it) }
        update.joinedToChat?.let { onJoinV1?.accept(it) }
        update.leftFromChat?.let { onLeaveV1?.accept(it) }
    }

    private fun dispatchV2(update: UpdateV2, botAccountId: AccountId?) {
        onUpdateV2?.accept(update)
        update.message?.let { msg ->
            if (botAccountId != null && msg.messageType == MessageType.USER) {
                // v2 не имеет прямого AccountId, фильтрация по botAccountId работает только для v1
                // TODO: добавить фильтрацию по MembershipId когда getMe() (v2) заработает
            }
            if (!tryDispatchCommand(msg.content.text, msg, commandsV2)) {
                onMessageV2?.accept(msg)
            }
        }
        update.notification?.let { onNotificationV2?.accept(it) }
        update.messageAction?.let { onMessageActionV2?.accept(it) }
        update.workspaceInvite?.let { onWorkspaceInviteV2?.accept(it) }
    }

    private fun <T> tryDispatchCommand(
        text: String?,
        payload: T,
        commands: Map<String, BiConsumer<T, List<String>>>
    ): Boolean {
        if (text == null || commands.isEmpty()) return false
        val trimmed = text.trimStart()
        if (!trimmed.startsWith("/")) return false
        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].removePrefix("/")
        val handler = commands[cmd] ?: return false
        handler.accept(payload, parts.drop(1))
        return true
    }
}
