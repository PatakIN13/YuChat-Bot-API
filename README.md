# YuChat Bot API SDK

[![Maven Central](https://img.shields.io/maven-central/v/space.patakin.yuchatbotapi/yuchatbotapi-core)](https://central.sonatype.com/namespace/space.patakin.yuchatbotapi)
[![API Docs](https://img.shields.io/badge/docs-Dokka-blue)](https://patakin13.github.io/YuChat-Bot-API/)

Kotlin SDK для работы с [YuChat](https://yuchat.ai) Bot API — корпоративным мессенджером.

Библиотека оборачивает HTTP API (v1 + v2) в типобезопасный, идиоматичный Kotlin-клиент с поддержкой long-polling и webhook.

## Модули

Библиотека разделена на независимые модули — подключайте только то, что нужно вашему проекту:

| Артефакт | Обязательный | Описание |
|---|---|---|
| `ru.rt.yuchatbotapi:yuchatbotapi-core` | ✅ да | Модели, HTTP-клиент, все API-методы (messages, chats, members, files, updates, webhooks, bot) |
| `ru.rt.yuchatbotapi:yuchatbotapi-java` | ❌ нет | Java-обёртка: все методы возвращают `CompletableFuture` вместо `suspend` |
| `ru.rt.yuchatbotapi:yuchatbotapi-polling` | ❌ нет | Long-polling с автоматическим управлением offset и DSL-диспетчер обновлений |
| `ru.rt.yuchatbotapi:yuchatbotapi-webhook` | ❌ нет | Встроенный Ktor-сервер для приёма webhook-обновлений |

**`core`** — единственный обязательный модуль. Он содержит все модели данных, HTTP-клиент и методы для вызова API. С ним одним можно отправлять сообщения, управлять чатами, участниками, файлами и даже вручную вызывать `getUpdates`.

**`java-api`** — добавьте вместо `core`, если пишете бота на Java. Оборачивает все suspend-методы в `CompletableFuture`, предоставляет `YuChatBotJavaClient` с удобным API.

**`polling`** — добавьте, если хотите получать обновления через long-polling с готовым циклом опроса, автоматическим offset и удобным DSL (`onMessage`, `onInvite` и т.д.). Если вы реализуете свой цикл опроса или используете webhook — этот модуль не нужен.

**`webhook`** — добавьте, если бот получает обновления через webhook. Модуль поднимает Ktor HTTP-сервер, валидирует secret token и парсит входящие обновления. Если вы используете polling или уже имеете свой HTTP-сервер (Spring, Ktor) — этот модуль не нужен, обрабатывайте webhook-запросы самостоятельно через модели из `core`.

## Подключение

```kotlin
// settings.gradle.kts — для локальной разработки через composite build
includeBuild("/path/to/yuchatbotapi")
```

```kotlin
// build.gradle.kts
dependencies {
    // Для Kotlin — модели, клиент, все API-методы
    implementation("ru.rt.yuchatbotapi:yuchatbotapi-core:0.2.0")

    // Для Java — вместо core, все методы через CompletableFuture
    implementation("ru.rt.yuchatbotapi:yuchatbotapi-java:0.2.0")

    // Опционально — выберите один из способов получения обновлений:
    implementation("ru.rt.yuchatbotapi:yuchatbotapi-polling:0.2.0")   // long-polling + DSL диспетчер
    implementation("ru.rt.yuchatbotapi:yuchatbotapi-webhook:0.2.0")    // встроенный webhook-сервер
}
```

## Quick Start

### Настройка токена

Токен бота получается через команду `/botcreate` боту `aibot@yuchat.ai` в YuChat.

Скопируйте файл-пример и укажите свой токен:
```bash
cp bot.properties.example bot.properties
# Отредактируйте bot.properties — вставьте токен бота
```

Содержимое `bot.properties`:
```properties
yuchat.bot.token=your-bot-token-here
# yuchat.base.url=https://yuchat.ai
```

> ⚠️ Файл `bot.properties` добавлен в `.gitignore` — токен не попадёт в репозиторий.

### Создание клиента

**Kotlin:**
```kotlin
import ru.rt.yuchatbotapi.api.YuChatBotClient

val bot = YuChatBotClient("your-jwt-token") {
    baseUrl = "https://yuchat.ai"   // по умолчанию
    maxRetries = 3                  // повтор при 429 Too Many Requests
}
```

**Java:**
```java
import ru.rt.yuchatbotapi.java.YuChatBotJavaClient;

YuChatBotJavaClient bot = new YuChatBotJavaClient("your-jwt-token");
// или с параметрами:
YuChatBotJavaClient bot = new YuChatBotJavaClient("your-jwt-token", "https://yuchat.ai", 3, 1000L);
```

### Отправка сообщения

**Kotlin:**
```kotlin
import ru.rt.yuchatbotapi.model.*

// v1 (стабильный)
bot.messages.send(
    workspaceId = WorkspaceId("workspace-id"),
    chatId = ChatId("chat-id"),
    text = "Привет! 👋"
)
```

> 💡 SDK использует [value class](https://kotlinlang.org/docs/inline-classes.html) для ID — `WorkspaceId`, `ChatId`, `ChatMessageId`, `AccountId`, `MembershipId`. Это защита от перепутывания параметров на этапе компиляции. При этом в рантайме это обычный `String` (zero-cost).

**Java:**
```java
// Java API принимает обычный String — value classes прозрачны
bot.messages.send("workspace-id", "chat-id", "Привет! 👋")
    .thenAccept(msg -> System.out.println("Sent: " + msg.getMessageId()));

// Или синхронно:
SendMessageResponse msg = bot.messages.send("workspace-id", "chat-id", "Привет!").join();
```

**Kotlin v2 (расширенный — с кнопками):**
```kotlin
import ru.rt.yuchatbotapi.model.*

bot.messages.sendV2(
    workspaceId = WorkspaceId("workspace-id"),
    chatId = ChatId("chat-id"),
    text = "Выберите действие",
    buttonBar = buttonBar {
        row {
            command("Помощь", "help")
            command("Отмена", "cancel")
        }
        row {
            link("Документация", "https://docs.example.com")
        }
    }
)
```

### Работа с чатами

```kotlin
// Создание воркспейс-чата (v1)
bot.chats.createWorkspace(
    workspaceId = WorkspaceId("ws-id"),
    name = "Новый чат",
    description = "Описание"
)

// Информация о чате (v2)
val info = bot.chats.getInfo(
    workspaceId = WorkspaceId("ws-id"),
    chatIds = listOf(ChatId("chat-id"))
)

// Архивация (v2)
bot.chats.archive(
    workspaceId = WorkspaceId("ws-id"),
    chatId = ChatId("chat-id")
)
```

### Работа с файлами

```kotlin
// Загрузка файла через высокоуровневый helper
val fileId = bot.files.upload(
    workspaceId = WorkspaceId("ws-id"),
    file = File("report.pdf")
)

// Отправка сообщения с файлом
bot.messages.send(
    workspaceId = WorkspaceId("ws-id"),
    chatId = ChatId("chat-id"),
    text = "Отчёт",
    fileIds = listOf(fileId)
)
```

### Информация о боте

```kotlin
val me = bot.bot.getMe()  // v2
println("Bot: ${me.profile.fullName}, workspaces: ${me.workspaces}")
println("Scope: ${me.scope.type}, API version: ${me.updateApiVersion}")
```

## Long Polling

```kotlin
import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.runPollingBot

// Минимальный бот — конфигурация загружается автоматически из bot.properties
fun main() = runPollingBot(PollingOptions(apiVersion = 1)) { client ->
    onMessage { msg ->
        msg.answer(client, "Echo: ${msg.text}")
    }

    onCommand("help") { msg, args ->
        msg.answer(client, "Справка: ...")
    }

    onInvite { invite ->
        println("Invited to chat: ${invite.chatId}")
    }

    onError { e ->
        System.err.println("Polling error: ${e.message}")
    }
}
```

### Polling v2 (расширенные обновления)

```kotlin
runPollingBot(PollingOptions(apiVersion = 2)) { client ->
    onMessageV2 { msg ->
        println("New message: ${msg.text}")  // shortcut для msg.content.text
    }

    onNotification { notification ->
        when {
            notification.chatArchivedEvent != null ->
                println("Chat archived: ${notification.chatArchivedEvent!!.chatId}")
        }
    }

    onMessageAction { action ->
        println("Button pressed: ${action.pressedButtonCommand?.commandKey}")
    }
}
```

### Настройки polling (PollingOptions)

```kotlin
PollingOptions(
    apiVersion = 1,           // версия API (1 или 2)
    limit = 20,               // количество обновлений за запрос
    pollDelayMs = 500L,       // задержка между запросами (мс)
    errorDelayMs = 5000L,     // задержка при ошибке (мс)
    autoConfigureV2 = true,   // авто-настройка updateSettings для v2
    skipPending = false        // пропустить накопленные обновления при старте
)
```

**`skipPending = true`** — при запуске бот пропустит все накопленные обновления и начнёт обрабатывать только новые. Полезно, когда бот перезапускается и не нужно отвечать на старые сообщения.

## Webhook

```kotlin
import ru.rt.yuchatbotapi.api.answer
import ru.rt.yuchatbotapi.webhook.WebhookServer

val bot = YuChatBotClient("token")
val server = WebhookServer(
    client = bot,
    port = 8443,
    path = "/webhook",
    secretToken = "my-secret"
)

// DSL-обработчики (как в polling)
server.handlers {
    onMessage { msg ->
        msg.answer(bot, "Got it!")
    }

    onCommand("help") { msg, _ ->
        msg.answer(bot, "Справка...")
    }

    onError { e ->
        System.err.println("Webhook error: ${e.message}")
    }
}

server.start(
    webhookUrl = "https://mybot.example.com/webhook",
    wait = true
)
```

## Удобства SDK

### Типобезопасные ID (value classes)

SDK использует `@JvmInline value class` для всех идентификаторов:

| Тип | Описание |
|---|---|
| `WorkspaceId` | ID воркспейса |
| `ChatId` | ID чата |
| `ChatMessageId` | ID сообщения |
| `AccountId` | ID аккаунта пользователя |
| `MembershipId` | ID участника в воркспейсе |

```kotlin
// Компилятор не позволит перепутать параметры:
bot.messages.send(
    workspaceId = WorkspaceId("ws-123"),
    chatId = ChatId("chat-456"),
    text = "Привет!"
)
// bot.messages.send(ChatId("chat"), WorkspaceId("ws"), "text") — ❌ ошибка компиляции!

// В хендлерах ID приходят уже в нужном типе:
onMessage { msg ->
    val ws: WorkspaceId = msg.workspaceId    // уже WorkspaceId
    val chat: ChatId = msg.chatId            // уже ChatId
    msg.answer(client, "OK")                 // передаются автоматически
}

// Конвертация из/в String:
val wsId = WorkspaceId("raw-string")     // String → WorkspaceId
val raw: String = wsId.value             // WorkspaceId → String
println(wsId)                            // выведет "raw-string" (toString = value)
```

**Java:** value classes прозрачны — геттеры возвращают `String`, Java API принимает `String`:
```java
String wsId = msg.getWorkspaceId();   // String, не WorkspaceId
bot.messages.send(wsId, chatId, "text");  // обычные строки
```

### BotConfig — автоматическая загрузка конфигурации

```kotlin
import ru.rt.yuchatbotapi.api.BotConfig

val token = BotConfig.requireToken()         // из env или bot.properties
val client = BotConfig.createClient()        // автоматически настроенный клиент
val custom = BotConfig.env("MY_VAR", "my.prop")  // произвольные параметры
```

### WorkspaceScope — без повторения workspaceId

```kotlin
val ws = client.workspace(WorkspaceId("workspace-id"))
ws.sendMessage(ChatId("chat-id"), "Привет!")
ws.listMembers()
ws.uploadFile(file)
```

### reply() / answer() — ответ на сообщение

```kotlin
// В хендлерах value classes передаются автоматически из модели —
// не нужно создавать WorkspaceId/ChatId вручную
onMessage { msg ->
    msg.reply(client, "Ответ с цитированием")   // reply с replyTo
    msg.answer(client, "Просто в тот же чат")    // без цитирования
}
```

### Пагинация через Flow

```kotlin
import ru.rt.yuchatbotapi.api.getWorkspaceChatsFlow

client.chats.getWorkspaceChatsFlow(WorkspaceId("ws-id")).collect { chatId ->
    println(chatId) // chatId: ChatId
}
```

## Архитектура API

SDK предоставляет **единый плоский API**. Пользователь не думает о версиях:

| Метод | Внутренний вызов | Описание |
|---|---|---|
| `messages.send()` | v1 | Отправка (стабильный) |
| `messages.sendV2()` | v2 | Отправка с расширениями |
| `messages.pin()` | v2 | Закрепление (только v2) |
| `messages.toggleReaction()` | v2 | Реакция (только v2) |
| `chats.createWorkspace()` | v1 | Создание чата |
| `chats.archive()` | v2 | Архивация (только v2) |
| `members.list()` | v1 | Список участников |
| `members.invite()` | v2 | Приглашение (только v2) |
| `bot.getMe()` | v2 | Информация о боте |

- Пересекающиеся методы → v1 (стабильный) по умолчанию, `V2`-суффикс для v2
- Новые v2-only методы → без суффикса
- При стабилизации v2: `send()` переключится на v2, `sendV2()` → `@Deprecated`

## Полный список API

### `bot.messages`
`send`, `edit`, `delete`, `forward` (v1) · `sendV2`, `editV2`, `deleteV2`, `forwardV2` (v2) · `pin`, `unpin`, `toggleReaction`, `getMessages`, `getById` (v2)

### `bot.chats`
`createWorkspace`, `createPersonal`, `createThread`, `listWorkspace`, `invite`, `kick` (v1) · `getWorkspaceChats`, `getMyChats`, `getInfo`, `leave`, `archive`, `unarchive`, `setMemberRole`, `inviteV2`, `kickV2`, `createUserEventsChat`, `createWorkspaceChatV2`, `getOrCreatePersonalChat`, `getOrCreateThreadChat` (v2)

### `bot.members`
`list` (v1) · `getMembers`, `invite`, `remove`, `setRole`, `getInfo` (v2)

### `bot.files`
`getUploadUrl`, `getDownloadUrl` (v1) · `upload` (helper) · `getUploadUrlV2`, `getDownloadUrlV2` (v2)

### `bot.updates`
`getUpdates` (v1) · `getUpdatesV2`, `setUpdateSettings`, `getWorkspaceInvites`, `acceptWorkspaceInvite`, `rejectAllWorkspaceInvites` (v2)

### `bot.webhooks`
`setWebhook`, `deleteWebhook`, `getWebhookInfo` (v1) · `setWebhookV2`, `deleteWebhookV2`, `getWebhookInfoV2` (v2)

### `bot.bot`
`getMe` (v2)

## Обработка ошибок

```kotlin
import ru.rt.yuchatbotapi.exception.*

try {
    bot.messages.send(
        workspaceId = WorkspaceId("ws"),
        chatId = ChatId("chat"),
        text = "test"
    )
} catch (e: AuthenticationException) {
    // 401 — невалидный токен
} catch (e: RateLimitException) {
    // 429 — превышен лимит (авто-retry настраивается через maxRetries)
} catch (e: YuChatApiException) {
    // Другие ошибки API (403, 500 и т.д.)
    println("Error ${e.statusCode}: ${e.message}")
}
```

## Требования

- JDK 11+
- Kotlin 1.9+

## Лицензия

Internal / proprietary.
