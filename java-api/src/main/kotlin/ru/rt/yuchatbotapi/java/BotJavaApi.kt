package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.BotApi
import ru.rt.yuchatbotapi.model.MeInfo
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.BotApi}.
 */
class BotJavaApi internal constructor(private val api: BotApi) {

    fun getMe(): CompletableFuture<MeInfo> = async {
        api.getMe()
    }
}
