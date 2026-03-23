package ru.rt.yuchatbotapi.exception

/**
 * Базовое исключение YuChat Bot API.
 *
 * @property statusCode HTTP-код ответа (если доступен)
 */
open class YuChatApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Ошибка аутентификации (HTTP 401).
 *
 * Возникает при невалидном или истёкшем JWT-токене.
 */
class AuthenticationException(
    message: String = "Authentication failed",
    statusCode: Int = 401
) : YuChatApiException(message, statusCode)

/**
 * Превышение лимита запросов (HTTP 429).
 *
 * Клиент автоматически повторяет запрос с экспоненциальной задержкой
 * (настраивается через [ru.rt.yuchatbotapi.api.ClientConfig.maxRetries]).
 *
 * @property retryAfterMs рекомендуемая задержка перед повтором (мс)
 */
class RateLimitException(
    message: String = "Rate limit exceeded",
    val retryAfterMs: Long? = null
) : YuChatApiException(message, 429)
