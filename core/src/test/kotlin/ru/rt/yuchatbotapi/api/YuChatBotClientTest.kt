package ru.rt.yuchatbotapi.api

import kotlin.test.Test

class YuChatBotClientTest {

    @Test
    fun `client creates with default config`() {
        val client = YuChatBotClient("token")
        client.close()
    }

    @Test
    fun `client creates with custom config`() {
        val client = YuChatBotClient("token") {
            baseUrl = "https://custom.yuchat.ai"
            maxRetries = 5
            retryDelayMs = 2000L
        }
        client.close()
    }

    @Test
    fun `client exposes all api groups`() {
        val client = YuChatBotClient("token")
        client.messages
        client.chats
        client.members
        client.files
        client.updates
        client.webhooks
        client.bot
        client.close()
    }

    @Test
    fun `client is closeable`() {
        val client = YuChatBotClient("token")
        client.use {
            it.messages
        }
    }
}
