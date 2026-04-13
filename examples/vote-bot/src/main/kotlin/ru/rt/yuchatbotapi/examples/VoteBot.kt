package ru.rt.yuchatbotapi.examples

import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.api.reply
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.model.ChatMessageId
import ru.rt.yuchatbotapi.model.WorkspaceId
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.runPollingBot
import java.util.concurrent.ConcurrentHashMap

/**
 * Пример бота для голосований (v1 API).
 *
 * Команды:
 * - `/poll Вопрос | Вариант 1 | Вариант 2 | ...` — создать голосование
 * - `/vote N` — проголосовать за вариант N
 * - Ответ (reply) на сообщение голосования с номером варианта — альтернативный способ голосования
 * - `/results` — показать результаты последнего голосования в чате
 * - `/help` — справка по командам
 *
 * Сообщение голосования обновляется в реальном времени при каждом голосе.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите токен бота
 */

data class Poll(
    val question: String,
    val options: List<String>,
    val workspaceId: WorkspaceId,
    val chatId: ru.rt.yuchatbotapi.model.ChatId,
    val messageId: ChatMessageId,
    // option index -> set of author ID strings
    val votes: ConcurrentHashMap<Int, MutableSet<String>> = ConcurrentHashMap()
)

// chatId -> active poll
private val activePolls = ConcurrentHashMap<String, Poll>()

// pollMessageId -> chatId (для поиска голосования по reply)
private val pollMessageIndex = ConcurrentHashMap<String, String>()

private fun formatPollText(poll: Poll): String {
    val totalVotes = poll.votes.values.sumOf { it.size }
    val lines = poll.options.mapIndexed { i, option ->
        val count = poll.votes[i]?.size ?: 0
        val pct = if (totalVotes > 0) count * 100 / totalVotes else 0
        val bar = "█".repeat(pct / 5) + "░".repeat(20 - pct / 5)
        "${i + 1}. $option  $bar  $count ($pct%)"
    }
    return "📊 **${poll.question}**\n\n${lines.joinToString("\n")}\n\n" +
        "Всего голосов: $totalVotes\n" +
        "_Голосуйте командой_ `/vote N` _или ответьте на это сообщение номером варианта_"
}

private suspend fun updatePollMessage(client: YuChatBotClient, poll: Poll) {
    client.messages.edit(
        workspaceId = poll.workspaceId,
        chatId = poll.chatId,
        messageId = poll.messageId,
        text = formatPollText(poll)
    )
}

fun main() = runPollingBot(PollingOptions(apiVersion = 1, skipPending = true)) { client ->

    onCommand("poll") { msg, args ->
        val raw = args.joinToString(" ")
        val parts = raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }

        if (parts.size < 3) {
            msg.answer(client, "Использование: `/poll Вопрос | Вариант 1 | Вариант 2 [| ...]`")
            return@onCommand
        }

        val question = parts[0]
        val options = parts.drop(1)

        val optionsList = options.mapIndexed { i, option ->
            "${i + 1}. $option  ░░░░░░░░░░░░░░░░░░░░  0 (0%)"
        }.joinToString("\n")

        msg.answer(client,
            "📊 **$question**\n\n$optionsList\n\nВсего голосов: 0\n" +
            "_Голосуйте командой_ `/vote N` _или ответьте на это сообщение номером варианта_"
        ).also { resp ->
            activePolls[msg.chatId.value] = Poll(
                question = question, options = options,
                workspaceId = msg.workspaceId, chatId = msg.chatId,
                messageId = resp.messageId
            )
            pollMessageIndex[resp.messageId.value] = msg.chatId.value
        }
        println("Poll created in chat ${msg.chatId}: $question")
    }

    onCommand("vote") { msg, args ->
        val poll = activePolls[msg.chatId.value]
        if (poll == null) {
            msg.answer(client, "Нет активного голосования. Создайте: `/poll Вопрос | Вариант 1 | Вариант 2`")
            return@onCommand
        }

        val optionIndex = args.firstOrNull()?.toIntOrNull()?.minus(1)
        if (optionIndex == null || optionIndex !in poll.options.indices) {
            msg.answer(client, "Укажите номер варианта от 1 до ${poll.options.size}")
            return@onCommand
        }

        val voterId = msg.author.value

        // Снимаем предыдущий голос
        poll.votes.values.forEach { it.remove(voterId) }

        // Добавляем голос за выбранный вариант
        poll.votes.computeIfAbsent(optionIndex) { ConcurrentHashMap.newKeySet() }.add(voterId)

        val count = poll.votes[optionIndex]?.size ?: 0
        updatePollMessage(client, poll)
        msg.answer(client, "✅ Голос принят: **${poll.options[optionIndex]}** ($count)")
        println("Vote for '${poll.options[optionIndex]}' by ${msg.author} in chat ${msg.chatId}")
    }

    onCommand("results") { msg, _ ->
        val poll = activePolls[msg.chatId.value]
        if (poll == null) {
            msg.answer(client, "Нет активного голосования. Создайте: `/poll Вопрос | Вариант 1 | Вариант 2`")
            return@onCommand
        }

        msg.answer(client, formatPollText(poll))
    }

    onCommand("help") { msg, _ ->
        msg.answer(client, """
            |📋 **Бот для голосований — команды:**
            |
            |`/poll Вопрос | Вариант 1 | Вариант 2 [| ...]` — создать голосование
            |`/vote N` — проголосовать за вариант N
            |`/results` — показать результаты
            |`/help` — эта справка
            |
            |💡 Также можно ответить (reply) на сообщение голосования номером варианта
        """.trimMargin())
    }

    onMessage { msg ->
        // Голосование через reply на сообщение голосования
        val parentId = msg.parentMessageId?.value ?: return@onMessage
        val chatId = pollMessageIndex[parentId] ?: return@onMessage
        val poll = activePolls[chatId] ?: return@onMessage

        val optionIndex = msg.text.trim().toIntOrNull()?.minus(1) ?: return@onMessage
        if (optionIndex !in poll.options.indices) return@onMessage

        val voterId = msg.author.value
        poll.votes.values.forEach { it.remove(voterId) }
        poll.votes.computeIfAbsent(optionIndex) { ConcurrentHashMap.newKeySet() }.add(voterId)

        val count = poll.votes[optionIndex]?.size ?: 0
        updatePollMessage(client, poll)
        msg.reply(client, "✅ Голос принят: **${poll.options[optionIndex]}** ($count)")
        println("Vote (reply) for '${poll.options[optionIndex]}' by ${msg.author} in chat ${msg.chatId}")
    }

    onInvite { invite ->
        println("Invited to chat ${invite.chatId} by ${invite.inviter}")
    }

    onError { e ->
        System.err.println("Error: ${e.message}")
    }
}
