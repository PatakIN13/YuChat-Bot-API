package ru.rt.yuchatbotapi.examples

import ru.rt.yuchatbotapi.api.BotConfig
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.runPollingBot

/**
 * Пример бота для работы с файлами.
 *
 * Бот обрабатывает команды:
 * - /upload — демонстрирует загрузку файла в чат
 * - /help — показывает справку
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите токен бота
 */
fun main() = runPollingBot(PollingOptions(apiVersion = 1)) { client ->
    onCommand("help") { msg, _ ->
        msg.answer(client, """
            **Команды:**
            `/upload` — загрузить тестовый файл в чат
            `/help` — эта справка
        """.trimIndent())
    }

    onCommand("upload") { msg, _ ->
        try {
            val content = "Hello from YuChat Bot SDK!\nTimestamp: ${System.currentTimeMillis()}"
            val tempFile = java.io.File.createTempFile("hello", ".txt")
            tempFile.writeBytes(content.toByteArray())

            val fileId = client.files.upload(
                workspaceId = msg.workspaceId,
                file = tempFile
            )
            println("File uploaded, fileId: $fileId")
            tempFile.delete()

            msg.answer(client, "✅ Файл `hello.txt` загружен! (fileId: $fileId)")
        } catch (e: Exception) {
            msg.answer(client, "❌ Ошибка загрузки: ${e.message}")
        }
    }

    onError { e ->
        System.err.println("Error: ${e.message}")
    }
}
