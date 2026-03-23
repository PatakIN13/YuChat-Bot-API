package ru.rt.yuchatbotapi.examples

import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.startPolling
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Properties

private val props = Properties().apply {
    File("bot.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

private fun env(name: String, prop: String): String? =
    System.getenv(name) ?: props.getProperty(prop)

/**
 * Пример echo-бота: повторяет каждое полученное сообщение.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите токен бота
 */
fun main() = runBlocking {
    val token = env("YUCHAT_BOT_TOKEN", "yuchat.bot.token")
        ?: error("Set YUCHAT_BOT_TOKEN env or yuchat.bot.token in bot.properties")

    val baseUrl = env("YUCHAT_BASE_URL", "yuchat.base.url")

    val client = YuChatBotClient(token) {
        if (!baseUrl.isNullOrBlank()) {
            this.baseUrl = baseUrl
        }
    }

    println("Echo bot started. Waiting for messages...")

    val polling = client.startPolling(PollingOptions(apiVersion = 1)) {
        onMessage { update ->
            val msg = update.newChatMessage ?: return@onMessage
            println("Received: ${msg.text} from ${msg.author} in ${msg.chatId}")

            client.messages.send(
                workspaceId = msg.workspaceId,
                chatId = msg.chatId,
                text = "Echo: ${msg.text}"
            )
        }

        onInvite { update ->
            val invite = update.inviteToChat ?: return@onInvite
            println("Invited to chat ${invite.chatId} by ${invite.inviter}")
        }

        onError { e ->
            System.err.println("Error: ${e.message}")
        }
    }

    // Ожидание завершения
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
        polling.stop()
        client.close()
    })

    // Блокируем main thread
    Thread.currentThread().join()
}
