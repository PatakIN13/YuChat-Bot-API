package ru.rt.yuchatbotapi.polling

/**
 * Настройки long-polling.
 */
data class PollingOptions(
    /** Версия API для получения обновлений (1 или 2) */
    val apiVersion: Int = 1,
    /** Количество обновлений за один запрос */
    val limit: Int = 20,
    /** Задержка между запросами при отсутствии обновлений (мс) */
    val pollDelayMs: Long = 500L,
    /** Задержка при ошибке (мс) */
    val errorDelayMs: Long = 5000L,
    /** Автоматически вызвать setUpdateSettings при apiVersion=2 */
    val autoConfigureV2: Boolean = true
)
