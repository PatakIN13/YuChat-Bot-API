package ru.rt.yuchatbotapi.examples

import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.webhook.WebhookServer
import java.io.File
import java.util.Properties

private val props = Properties().apply {
    File("bot.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

private fun env(name: String, prop: String): String? =
    System.getenv(name) ?: props.getProperty(prop)

/**
 * Пример webhook-бота: принимает обновления через HTTP и отвечает эхом.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите параметры.
 *
 * Необходимые параметры:
 * - yuchat.bot.token — токен бота
 * - yuchat.webhook.url — публичный URL, доступный для YuChat (например https://example.com/webhook)
 *
 * Опциональные:
 * - yuchat.base.url — базовый URL API (по умолчанию https://yuchat.ai)
 * - yuchat.webhook.port — порт сервера (по умолчанию 8080)
 * - yuchat.webhook.path — путь для webhook (по умолчанию /webhook)
 * - yuchat.webhook.secret — секретный токен для верификации запросов
 */
fun main() = runBlocking {
    val token = env("YUCHAT_BOT_TOKEN", "yuchat.bot.token")
        ?: error("Set YUCHAT_BOT_TOKEN env or yuchat.bot.token in bot.properties")

    val baseUrl = env("YUCHAT_BASE_URL", "yuchat.base.url")
    val webhookUrl = env("YUCHAT_WEBHOOK_URL", "yuchat.webhook.url")
    val port = env("YUCHAT_WEBHOOK_PORT", "yuchat.webhook.port")?.toIntOrNull() ?: 8080
    val path = env("YUCHAT_WEBHOOK_PATH", "yuchat.webhook.path") ?: "/webhook"
    val secret = env("YUCHAT_WEBHOOK_SECRET", "yuchat.webhook.secret")

    val client = YuChatBotClient(token) {
        if (!baseUrl.isNullOrBlank()) {
            this.baseUrl = baseUrl
        }
    }

    val server = WebhookServer(
        client = client,
        port = port,
        path = path,
        secretToken = secret,
        apiVersion = 1
    )

    server.onUpdateV1 { update ->
        val msg = update.newChatMessage ?: return@onUpdateV1
        println("Received: ${msg.text} from ${msg.author} in ${msg.chatId}")

        client.messages.send(
            workspaceId = msg.workspaceId,
            chatId = msg.chatId,
            text = "Echo (webhook): ${msg.text}"
        )
    }

    server.onError { e ->
        System.err.println("Webhook error: ${e.message}")
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
        runBlocking { server.stop() }
        client.close()
    })

    println("Webhook bot starting on port $port, path: $path")
    server.start(webhookUrl = webhookUrl, wait = true)
}
