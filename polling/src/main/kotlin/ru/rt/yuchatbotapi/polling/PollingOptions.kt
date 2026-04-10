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
     * AccountId бота для фильтрации собственных сообщений.
     *
     * Если задан, обработчики [onMessage]/[onCommand] автоматически пропускают
     * сообщения, отправленные самим ботом. Обработчик [onUpdate] (raw)
     * по-прежнему получает все обновления.
     *
     * Получить ID можно через `/public/v1/member.list` — в ответе найти бота
     * по имени и взять `profileId`.
     */
    val botAccountId: ru.rt.yuchatbotapi.model.AccountId? = null,
    /**
     * Автоматическое определение ID бота через `getMe()` (v2 API).
     *
     * Если `true`, при старте бот пытается вызвать `getMe()` и получить свой `AccountId`.
     * При успехе — фильтрация собственных сообщений включается автоматически.
     * При ошибке (v2 API недоступен) — логируется предупреждение и используется
     * ручной [botAccountId], если он задан.
     *
     * **Рекомендация:** сейчас v2 API может быть недоступен, поэтому рекомендуется
     * использовать оба параметра вместе: `autoResolveBotId = true` + `botAccountId = AccountId("...")`.
     * Когда v2 заработает, `botAccountId` можно будет убрать.
     *
     * @see botAccountId
     */
    val autoResolveBotId: Boolean = false
)
