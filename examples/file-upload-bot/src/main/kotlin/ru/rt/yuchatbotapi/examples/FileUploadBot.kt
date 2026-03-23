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
 * Пример бота для работы с файлами.
 *
 * Бот обрабатывает команды:
 * - /upload — демонстрирует загрузку файла в чат
 * - /help — показывает справку
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

    println("File upload bot started. Send /upload or /help")

    val polling = client.startPolling(PollingOptions(apiVersion = 1)) {
        onMessage { update ->
            val msg = update.newChatMessage ?: return@onMessage
            val text = msg.text.trim()

            when {
                text == "/help" -> {
                    client.messages.send(
                        workspaceId = msg.workspaceId,
                        chatId = msg.chatId,
                        text = """
                            **Команды:**
                            `/upload` — загрузить тестовый файл в чат
                            `/help` — эта справка
                        """.trimIndent()
                    )
                }

                text == "/upload" -> {
                    try {
                        val content = "Hello from YuChat Bot SDK!\nTimestamp: ${System.currentTimeMillis()}"
                        val bytes = content.toByteArray()
                        val tempFile = java.io.File.createTempFile("hello", ".txt")
                        tempFile.writeBytes(bytes)

                        // Загрузить файл через helper (getUploadUrl + PUT)
                        val fileId = client.files.upload(
                            workspaceId = msg.workspaceId,
                            file = tempFile
                        )
                        println("File uploaded, fileId: $fileId")

                        tempFile.delete()

                        client.messages.send(
                            workspaceId = msg.workspaceId,
                            chatId = msg.chatId,
                            text = "✅ Файл `hello.txt` загружен! (fileId: $fileId)"
                        )
                    } catch (e: Exception) {
                        client.messages.send(
                            workspaceId = msg.workspaceId,
                            chatId = msg.chatId,
                            text = "❌ Ошибка загрузки: ${e.message}"
                        )
                    }
                }
            }
        }

        onError { e ->
            System.err.println("Error: ${e.message}")
        }
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
        polling.stop()
        client.close()
    })

    Thread.currentThread().join()
}
