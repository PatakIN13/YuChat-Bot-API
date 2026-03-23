package ru.rt.yuchatbotapi.java

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.rt.yuchatbotapi.model.SetWebhookRequest
import ru.rt.yuchatbotapi.model.UpdateV1
import ru.rt.yuchatbotapi.model.UpdateV2
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Java-совместимый webhook-сервер для приёма обновлений от YuChat.
 *
 * <pre>{@code
 * YuChatBotJavaClient client = new YuChatBotJavaClient("token", "https://yuchat.ai");
 * WebhookJavaServer server = new WebhookJavaServer(client, 8080, "/webhook");
 *
 * server.onUpdateV1(update -> {
 *     var msg = update.getNewChatMessage();
 *     if (msg != null) {
 *         client.messages.send(msg.getWorkspaceId(), msg.getChatId(), "Echo: " + msg.getText()).join();
 *     }
 * });
 *
 * server.start("https://my-server.com/webhook", null, false);
 * }</pre>
 */
class WebhookJavaServer @JvmOverloads constructor(
    private val client: YuChatBotJavaClient,
    private val port: Int = 8080,
    private val path: String = "/webhook",
    private val secretToken: String? = null,
    private val apiVersion: Int = 1
) {
    private val logger = LoggerFactory.getLogger(WebhookJavaServer::class.java)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    private var handlerV1: Consumer<UpdateV1>? = null
    private var handlerV2: Consumer<UpdateV2>? = null
    private var errorHandler: Consumer<Throwable>? = null
    private var server: ApplicationEngine? = null

    /** Регистрация обработчика обновлений v1 */
    fun onUpdateV1(handler: Consumer<UpdateV1>) { handlerV1 = handler }

    /** Регистрация обработчика обновлений v2 */
    fun onUpdateV2(handler: Consumer<UpdateV2>) { handlerV2 = handler }

    /** Регистрация обработчика ошибок */
    fun onError(handler: Consumer<Throwable>) { errorHandler = handler }

    /**
     * Регистрирует webhook URL в YuChat и запускает HTTP-сервер.
     * @param webhookUrl публичный URL, доступный для YuChat (null — не регистрировать)
     * @param certificate Base64 PEM сертификат для самоподписанных (null — не нужен)
     * @param wait блокировать текущий поток
     */
    @JvmOverloads
    fun start(
        webhookUrl: String? = null,
        certificate: String? = null,
        wait: Boolean = false
    ) {
        val kotlinClient = client.kotlinClient()

        if (webhookUrl != null) {
            runBlocking {
                kotlinClient.webhooks.setWebhook(SetWebhookRequest(
                    url = webhookUrl,
                    certificate = certificate,
                    secretToken = secretToken
                ))
            }
            logger.info("Webhook registered: $webhookUrl")
        }

        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json(this@WebhookJavaServer.json)
            }
            routing {
                post(this@WebhookJavaServer.path) {
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
                            val update = this@WebhookJavaServer.json.decodeFromString<UpdateV2>(body)
                            handlerV2?.accept(update)
                        } else {
                            val update = this@WebhookJavaServer.json.decodeFromString<UpdateV1>(body)
                            handlerV1?.accept(update)
                        }
                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        logger.error("Error processing webhook", e)
                        errorHandler?.accept(e)
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
    @JvmOverloads
    fun stop(removeWebhook: Boolean = true) {
        if (removeWebhook) {
            try {
                runBlocking { client.kotlinClient().webhooks.deleteWebhook() }
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
