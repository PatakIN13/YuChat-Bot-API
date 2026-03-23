package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.WebhooksApi
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.WebhooksApi}.
 */
class WebhooksJavaApi internal constructor(private val api: WebhooksApi) {

    fun setWebhook(request: SetWebhookRequest): CompletableFuture<Void> = asyncVoid {
        api.setWebhook(request)
    }

    fun deleteWebhook(): CompletableFuture<Void> = asyncVoid {
        api.deleteWebhook()
    }

    fun getWebhookInfo(): CompletableFuture<WebhookInfo> = async {
        api.getWebhookInfo()
    }

    fun setWebhookV2(request: SetWebhookRequest): CompletableFuture<Void> = asyncVoid {
        api.setWebhookV2(request)
    }

    fun deleteWebhookV2(): CompletableFuture<Void> = asyncVoid {
        api.deleteWebhookV2()
    }

    fun getWebhookInfoV2(): CompletableFuture<WebhookInfo> = async {
        api.getWebhookInfoV2()
    }
}
