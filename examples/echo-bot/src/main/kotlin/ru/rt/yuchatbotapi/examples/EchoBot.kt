package ru.rt.yuchatbotapi.examples

import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.runPollingBot

/**
 * Пример echo-бота: повторяет каждое полученное сообщение.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите токен бота
 */
fun main() = runPollingBot(PollingOptions(apiVersion = 1)) { client ->
    onMessage { msg ->
        println("Received: ${msg.text} from ${msg.author} in ${msg.chatId}")
        msg.answer(client, "Echo: ${msg.text}")
    }

    onInvite { invite ->
        println("Invited to chat ${invite.chatId} by ${invite.inviter}")
    }

    onError { e ->
        System.err.println("Error: ${e.message}")
    }
}
