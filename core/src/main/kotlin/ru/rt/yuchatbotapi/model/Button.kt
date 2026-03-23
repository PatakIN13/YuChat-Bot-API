package ru.rt.yuchatbotapi.model

import kotlinx.serialization.Serializable

/**
 * Панель кнопок для сообщения (v2).
 *
 * Прикрепляется к сообщению через параметр `buttonBar` в [ru.rt.yuchatbotapi.api.MessagesApi.sendV2].
 * Кнопки организованы в строки ([ButtonGroup]), каждая строка содержит один или несколько [Button].
 */
@Serializable
data class ButtonBar(
    val buttonGroups: List<ButtonGroup>
)

/** Строка кнопок (горизонтальная группа). */
@Serializable
data class ButtonGroup(
    val buttons: List<Button>
)

/**
 * Кнопка сообщения (v2).
 *
 * Содержит ровно одну из: [commandButton] (вызывает команду) или [linkButton] (открывает ссылку).
 */
@Serializable
data class Button(
    val commandButton: CommandButton? = null,
    val linkButton: LinkButton? = null
)

/**
 * Кнопка-команда (v2).
 *
 * При нажатии генерирует [MessageAction] с `pressedButtonCommand.commandKey`.
 *
 * @property displayText текст на кнопке
 * @property commandKey идентификатор команды, приходящий в [MessageAction]
 */
@Serializable
data class CommandButton(
    val displayText: String,
    val commandKey: String
)

/**
 * Кнопка-ссылка (v2).
 *
 * При нажатии открывает URL в браузере.
 *
 * @property displayText текст на кнопке
 * @property link URL для открытия
 */
@Serializable
data class LinkButton(
    val displayText: String,
    val link: String
)
