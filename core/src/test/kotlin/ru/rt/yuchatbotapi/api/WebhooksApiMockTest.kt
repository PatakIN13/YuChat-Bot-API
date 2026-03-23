package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WebhooksApiMockTest {

    @Test
    fun `setWebhook v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = WebhooksApi(client)

        api.setWebhook(ru.rt.yuchatbotapi.model.SetWebhookRequest(
            url = "https://mybot.example.com/webhook",
            secretToken = "secret-123"
        ))

        assertEquals("/public/v1/bot.setWebhook", capturedPath)
    }

    @Test
    fun `deleteWebhook v1 calls correct endpoint with DELETE method`() = runBlocking {
        var capturedPath = ""
        var capturedMethod = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method.value
            emptyOkResponse()
        }
        val api = WebhooksApi(client)

        api.deleteWebhook()

        assertEquals("/public/v1/bot.deleteWebhook", capturedPath)
        assertEquals("DELETE", capturedMethod)
    }

    @Test
    fun `getWebhookInfo v1 calls correct endpoint with GET method`() = runBlocking {
        var capturedPath = ""
        var capturedMethod = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method.value
            jsonResponse("""{"webhookInfo":{
                "url":"https://mybot.example.com/webhook",
                "hasCustomCertificate":false,
                "pendingUpdateCount":5
            }}""")
        }
        val api = WebhooksApi(client)

        val result = api.getWebhookInfo()

        assertEquals("/public/v1/bot.getWebhookInfo", capturedPath)
        assertEquals("GET", capturedMethod)
        assertEquals("https://mybot.example.com/webhook", result.url)
        assertEquals(5, result.pendingUpdateCount)
    }

    @Test
    fun `setWebhookV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            emptyOkResponse()
        }
        val api = WebhooksApi(client)

        api.setWebhookV2(ru.rt.yuchatbotapi.model.SetWebhookRequest(url = "https://v2.example.com/wh"))

        assertEquals("/public/v2/setWebhook", capturedPath)
    }

    @Test
    fun `deleteWebhookV2 calls correct endpoint with DELETE method`() = runBlocking {
        var capturedPath = ""
        var capturedMethod = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method.value
            emptyOkResponse()
        }
        val api = WebhooksApi(client)

        api.deleteWebhookV2()

        assertEquals("/public/v2/deleteWebhook", capturedPath)
        assertEquals("DELETE", capturedMethod)
    }

    @Test
    fun `getWebhookInfoV2 calls correct endpoint with GET method`() = runBlocking {
        var capturedPath = ""
        var capturedMethod = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method.value
            jsonResponse("""{
                "url":"https://v2.example.com/wh",
                "hasCustomCertificate":true,
                "pendingUpdateCount":0
            }""")
        }
        val api = WebhooksApi(client)

        val result = api.getWebhookInfoV2()

        assertEquals("/public/v2/getWebhookInfo", capturedPath)
        assertEquals("GET", capturedMethod)
        assertEquals(true, result.hasCustomCertificate)
    }
}
