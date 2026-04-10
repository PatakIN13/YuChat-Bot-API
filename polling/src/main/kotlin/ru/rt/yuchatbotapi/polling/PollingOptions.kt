package ru.rt.yuchatbotapi.polling

/**
 * Настройки long-polling.
 *
 * @property apiVersion версия API для получения обновлений (1 или 2)
 * @property limit количество обновлений за один запрос
 * @property pollDelayMs задержка между запросами при отсутствии обновлений (мс)
 * @property errorDelayMs задержка при ошибке (мс)
 * @property autoConfigureV2 автоматически вызвать `setUpdateSettings` при `apiVersion=2`
 * @property skipPending если `true`, при старте polling пропустит все накопленные
 *   обновления (сдвинет offset до конца очереди). Полезно при перезапуске бота,
 *   чтобы не обрабатывать устаревшие сообщения.
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
    val autoConfigureV2: Boolean = true,
    /** Пропустить накопившиеся обновления при старте (сдвинуть offset до конца) */
    val skipPending: Boolean = false,
    /**
     * Игнорировать собственные сообщения бота.
     *
     * Если `true` (по умолчанию), при старте polling бот определяет свой ID
     * через `getMe()` и автоматически пропускает сообщения от самого себя
     * в обработчиках [onMessage]/[onCommand]. Обработчик [onUpdate] (raw)
     * по-прежнему получает все обновления.
     */
    val ignoreSelfMessages: Boolean = true
)
