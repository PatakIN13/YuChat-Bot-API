package ru.rt.yuchatbotapi.webhook

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.handler.UpdateDispatcher
import ru.rt.yuchatbotapi.model.SetWebhookRequest
import ru.rt.yuchatbotapi.model.UpdateV1
import ru.rt.yuchatbotapi.model.UpdateV2

/**
 * Webhook-сервер для приёма обновлений от YuChat.
 *
 * Пример:
 * ```kotlin
 * val bot = YuChatBotClient("token")
 * val server = WebhookServer(bot, port = 8443, path = "/webhook")
 *
 * server.onUpdateV1 { update ->
 *     val msg = update.newChatMessage ?: return@onUpdateV1
 *     bot.messages.send(msg.workspaceId, msg.chatId, "Echo: ${msg.text}")
 * }
 *
 * server.start(wait = true)
 * ```
 */
class WebhookServer(
    private val client: YuChatBotClient,
    private val port: Int = 8080,
    private val path: String = "/webhook",
    private val secretToken: String? = null,
    private val apiVersion: Int = 1,
    /**
     * Игнорировать собственные сообщения бота.
     *
     * Если `true` (по умолчанию), при старте сервера бот определяет свой ID
     * через `getMe()` и автоматически пропускает сообщения от самого себя
     * в обработчиках, зарегистрированных через [handlers].
     */
    private val ignoreSelfMessages: Boolean = true
) {
    private val logger = LoggerFactory.getLogger(WebhookServer::class.java)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    private var handlerV1: (suspend (UpdateV1) -> Unit)? = null
    private var handlerV2: (suspend (UpdateV2) -> Unit)? = null
    private var errorHandler: (suspend (Throwable) -> Unit)? = null
    private var dispatcher: UpdateDispatcher? = null
    private var server: ApplicationEngine? = null

    /** Регистрация обработчика обновлений v1 */
    fun onUpdateV1(handler: suspend (UpdateV1) -> Unit) {
        handlerV1 = handler
    }

    /** Регистрация обработчика обновлений v2 */
    fun onUpdateV2(handler: suspend (UpdateV2) -> Unit) {
        handlerV2 = handler
    }

    /** Регистрация обработчика ошибок */
    fun onError(handler: suspend (Throwable) -> Unit) {
        errorHandler = handler
    }

    /**
     * Регистрация обработчиков через DSL (аналогично polling).
     *
     * ```kotlin
     * server.handlers {
     *     onMessage { msg ->
     *         msg.reply(bot, "Echo: ${msg.text}")
     *     }
     *     onCommand("help") { msg, args ->
     *         msg.answer(bot, "Справка...")
     *     }
     *     onError { e -> e.printStackTrace() }
     * }
     * ```
     */
    fun handlers(configure: UpdateDispatcher.() -> Unit) {
        val d = UpdateDispatcher().apply(configure)
        dispatcher = d
        handlerV1 = { update -> d.dispatchV1(update) }
        handlerV2 = { update -> d.dispatchV2(update) }
        d.onError?.let { handler -> errorHandler = handler }
    }

    /**
     * Регистрирует webhook URL в YuChat и запускает HTTP-сервер.
     * @param webhookUrl публичный URL, доступный для YuChat
     * @param certificate Base64 PEM сертификат (для самоподписанных)
     * @param wait блокировать текущий поток
     */
    suspend fun start(
        webhookUrl: String? = null,
        certificate: String? = null,
        wait: Boolean = false
    ) {
        // Определяем ID бота для фильтрации собственных сообщений
        if (ignoreSelfMessages && dispatcher != null) {
            try {
                val meInfo = client.bot.getMe()
                dispatcher!!.botAccountId = meInfo.profile.accountId
                logger.info("Self-message filtering enabled (accountId={})", meInfo.profile.accountId)

                if (apiVersion == 2) {
                    val membershipIds = mutableSetOf<ru.rt.yuchatbotapi.model.MembershipId>()
                    for (ws in meInfo.workspaces) {
                        try {
                            val members = client.members.list(ws)
                            members.find { it.profile?.profileId == meInfo.profile.accountId.value }
                                ?.memberId?.let { membershipIds.add(it) }
                        } catch (e: Exception) {
                            logger.warn("Failed to resolve bot membershipId for workspace {}", ws, e)
                        }
                    }
                    dispatcher!!.botMembershipIds = membershipIds

                    val accountId = meInfo.profile.accountId
                    dispatcher!!.membershipResolver = { workspaceId ->
                        val members = client.members.list(workspaceId)
                        members.find { it.profile?.profileId == accountId.value }?.memberId
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to resolve bot identity, self-message filtering disabled", e)
            }
        }

        if (webhookUrl != null) {
            client.webhooks.setWebhook(SetWebhookRequest(
                url = webhookUrl,
                certificate = certificate,
                secretToken = secretToken
            ))
            logger.info("Webhook registered: $webhookUrl")
        }

        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json(this@WebhookServer.json)
            }
            routing {
                post(this@WebhookServer.path) {
                    if (secretToken != null) {
                        val headerToken = call.request.header("X-YuChat-Bot-Api-Secret-Token")
                        if (headerToken != secretToken) {
                            call.respond(HttpStatusCode.Unauthorized)
                            return@post
                        }
                    }

                    try {
                        val body = call.receiveText()
                        if (apiVersion == 2) {
                            val update = this@WebhookServer.json.decodeFromString<UpdateV2>(body)
                            handlerV2?.invoke(update)
                        } else {
                            val update = this@WebhookServer.json.decodeFromString<UpdateV1>(body)
                            handlerV1?.invoke(update)
                        }
                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        logger.error("Error processing webhook", e)
                        errorHandler?.invoke(e)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }

                get("/health") {
                    call.respond(HttpStatusCode.OK, "ok")
                }
            }
        }

        logger.info("Webhook server starting on port $port, path: $path")
        server!!.start(wait = wait)
    }

    /** Останавливает сервер и удаляет webhook */
    suspend fun stop(removeWebhook: Boolean = true) {
        if (removeWebhook) {
            try {
                client.webhooks.deleteWebhook()
                logger.info("Webhook removed")
            } catch (e: Exception) {
                logger.warn("Failed to remove webhook", e)
            }
        }
        server?.stop(1000, 2000)
        server = null
        logger.info("Webhook server stopped")
    }
}
