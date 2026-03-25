package ru.rt.yuchatbotapi.api

import java.io.File
import java.util.Properties

/**
 * Утилита для загрузки конфигурации бота.
 *
 * Поддерживает `bot.properties` файл и переменные окружения.
 * Переменные окружения имеют приоритет над properties.
 *
 * Пример:
 * ```kotlin
 * val token = BotConfig.requireToken()
 * val client = BotConfig.createClient()
 * ```
 */
object BotConfig {
    private val props by lazy {
        Properties().apply {
            File("bot.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
    }

    /**
     * Получение значения конфигурации.
     * Сначала проверяет переменную окружения, затем property.
     */
    fun env(envName: String, propName: String): String? =
        System.getenv(envName) ?: props.getProperty(propName)

    /** Получение токена бота (обязательный параметр). */
    fun requireToken(): String = env("YUCHAT_BOT_TOKEN", "yuchat.bot.token")
        ?: error("Set YUCHAT_BOT_TOKEN env or yuchat.bot.token in bot.properties")

    /** Базовый URL API (опциональный). */
    fun baseUrl(): String? = env("YUCHAT_BASE_URL", "yuchat.base.url")

    /** Создаёт настроенный клиент с автоматической загрузкой конфигурации. */
    fun createClient(configure: ClientConfig.() -> Unit = {}): YuChatBotClient =
        YuChatBotClient(requireToken()) {
            baseUrl()?.let { baseUrl = it }
            configure()
        }
}
