package ru.rt.yuchatbotapi.examples

import ru.rt.yuchatbotapi.api.BotConfig
import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.webhook.WebhookServer

/**
 * Пример webhook-бота: принимает обновления через HTTP и отвечает эхом.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите параметры.
 */
fun main() {
    val client = BotConfig.createClient()
    val webhookUrl = BotConfig.env("YUCHAT_WEBHOOK_URL", "yuchat.webhook.url")
    val port = BotConfig.env("YUCHAT_WEBHOOK_PORT", "yuchat.webhook.port")?.toIntOrNull() ?: 8080
    val path = BotConfig.env("YUCHAT_WEBHOOK_PATH", "yuchat.webhook.path") ?: "/webhook"
    val secret = BotConfig.env("YUCHAT_WEBHOOK_SECRET", "yuchat.webhook.secret")

    val server = WebhookServer(
        client = client,
        port = port,
        path = path,
        secretToken = secret,
        apiVersion = 1
    )

    server.handlers {
        onMessage { msg ->
            println("Received: ${msg.text} from ${msg.author} in ${msg.chatId}")
            msg.answer(client, "Echo (webhook): ${msg.text}")
        }

        onError { e ->
            System.err.println("Webhook error: ${e.message}")
        }
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
        kotlinx.coroutines.runBlocking { server.stop() }
        client.close()
    })

    println("Webhook bot starting on port $port, path: $path")
    kotlinx.coroutines.runBlocking {
        server.start(webhookUrl = webhookUrl, wait = true)
    }
}
