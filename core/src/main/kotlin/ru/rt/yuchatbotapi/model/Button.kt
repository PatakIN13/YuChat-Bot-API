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

// ── DSL Builder ──

/**
 * DSL для создания [ButtonBar].
 *
 * ```kotlin
 * val bar = buttonBar {
 *     row {
 *         command("Да", "confirm")
 *         command("Нет", "cancel")
 *     }
 *     row {
 *         link("Документация", "https://docs.example.com")
 *     }
 * }
 * ```
 */
fun buttonBar(init: ButtonBarBuilder.() -> Unit): ButtonBar =
    ButtonBarBuilder().apply(init).build()

/** Билдер для [ButtonBar]. */
class ButtonBarBuilder {
    private val groups = mutableListOf<ButtonGroup>()

    /** Добавляет горизонтальную строку кнопок. */
    fun row(init: ButtonGroupBuilder.() -> Unit) {
        groups += ButtonGroupBuilder().apply(init).build()
    }

    internal fun build() = ButtonBar(groups)
}

/** Билдер для строки кнопок ([ButtonGroup]). */
class ButtonGroupBuilder {
    private val buttons = mutableListOf<Button>()

    /** Кнопка-команда: при нажатии генерирует [MessageAction]. */
    fun command(text: String, key: String) {
        buttons += Button(commandButton = CommandButton(text, key))
    }

    /** Кнопка-ссылка: при нажатии открывает URL. */
    fun link(text: String, url: String) {
        buttons += Button(linkButton = LinkButton(text, url))
    }

    internal fun build() = ButtonGroup(buttons)
}
