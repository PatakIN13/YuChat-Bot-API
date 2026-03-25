package ru.rt.yuchatbotapi.examples

import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.api.YuChatBotClient
import ru.rt.yuchatbotapi.model.*
import ru.rt.yuchatbotapi.polling.PollingOptions
import ru.rt.yuchatbotapi.polling.startPolling
import java.io.File
import java.util.Properties

private val props = Properties().apply {
    File("bot.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

private fun env(name: String, prop: String): String? =
    System.getenv(name) ?: props.getProperty(prop)

/**
 * Тестовый бот для проверки ВСЕХ эндпоинтов API.
 *
 * Использование: отправьте команду в чат с ботом.
 * /help — список всех команд
 *
 * Бот автоматически использует workspaceId и chatId из входящего сообщения.
 */
fun main() = runBlocking {
    val token = env("YUCHAT_BOT_TOKEN", "yuchat.bot.token")
        ?: error("Set YUCHAT_BOT_TOKEN env or yuchat.bot.token in bot.properties")
    val baseUrl = env("YUCHAT_BASE_URL", "yuchat.base.url")

    val client = YuChatBotClient(token) {
        if (!baseUrl.isNullOrBlank()) this.baseUrl = baseUrl
    }

    println("Test bot started. Send /help to see commands.")

    val polling = client.startPolling(PollingOptions(apiVersion = 1)) {
        onMessage { msg ->
            val text = msg.text.trim()
            if (text.isBlank()) return@onMessage
            val ws = msg.workspaceId
            val chatId = msg.chatId
            val msgId = msg.messageId

            if (!text.startsWith("/")) return@onMessage

            val parts = text.split(" ", limit = 2)
            val cmd = parts[0].lowercase()
            val arg = parts.getOrNull(1)?.trim()

            try {
                when (cmd) {
                    "/help" -> showHelp(client, ws, chatId)

                    // ── Bot ──
                    "/getme" -> testGetMe(client, ws, chatId)

                    // ── Messages v1 ──
                    "/send" -> testSend(client, ws, chatId)
                    "/edit" -> testEdit(client, ws, chatId)
                    "/delete" -> testDelete(client, ws, chatId)
                    "/forward" -> testForward(client, ws, chatId, arg)

                    // ── Messages v2 ──
                    "/sendv2" -> testSendV2(client, ws, chatId)
                    "/editv2" -> testEditV2(client, ws, chatId)
                    "/deletev2" -> testDeleteV2(client, ws, chatId)
                    "/forwardv2" -> testForwardV2(client, ws, chatId, arg)
                    "/getmessages" -> testGetMessages(client, ws, chatId)
                    "/getmessagebyid" -> testGetMessageById(client, ws, chatId, msgId)
                    "/pin" -> testPin(client, ws, chatId, arg)
                    "/unpin" -> testUnpin(client, ws, chatId, arg)
                    "/reaction" -> testReaction(client, ws, chatId, msgId, arg)

                    // ── Chats v1 ──
                    "/createworkspace" -> testCreateWorkspaceChat(client, ws, chatId, arg)
                    "/createpersonal" -> testCreatePersonalChat(client, ws, chatId, arg)
                    "/createthread" -> testCreateThread(client, ws, chatId, msgId)
                    "/listworkspace" -> testListWorkspace(client, ws, chatId)
                    "/invite" -> testInviteV1(client, ws, chatId, arg)
                    "/kick" -> testKickV1(client, ws, chatId, arg)

                    // ── Chats v2 ──
                    "/getworkspacechats" -> testGetWorkspaceChats(client, ws, chatId)
                    "/getmychats" -> testGetMyChats(client, ws, chatId)
                    "/getchatsinfo" -> testGetChatsInfo(client, ws, chatId)
                    "/leavechat" -> testLeaveChat(client, ws, chatId, arg)
                    "/archivechat" -> testArchiveChat(client, ws, chatId, arg)
                    "/unarchivechat" -> testUnarchiveChat(client, ws, chatId, arg)
                    "/setmemberrole" -> testSetChatMemberRole(client, ws, chatId, arg)
                    "/invitev2" -> testInviteV2(client, ws, chatId, arg)
                    "/kickv2" -> testKickV2(client, ws, chatId, arg)
                    "/eventschat" -> testCreateEventsChat(client, ws, chatId)
                    "/createworkspacev2" -> testCreateWorkspaceChatV2(client, ws, chatId, arg)
                    "/personalbychat" -> testGetOrCreatePersonal(client, ws, chatId, arg)
                    "/threadv2" -> testGetOrCreateThread(client, ws, chatId, arg)

                    // ── Members ──
                    "/memberlist" -> testMemberList(client, ws, chatId)
                    "/getmembers" -> testGetMembers(client, ws, chatId)
                    "/invitemember" -> testInviteMember(client, ws, chatId, arg)
                    "/removemember" -> testRemoveMember(client, ws, chatId, arg)
                    "/setrole" -> testSetRole(client, ws, chatId, arg)
                    "/getmembersinfo" -> testGetMembersInfo(client, ws, chatId, arg)

                    // ── Files ──
                    "/uploadurl" -> testGetUploadUrl(client, ws, chatId)
                    "/downloadurl" -> testGetDownloadUrl(client, ws, chatId, arg)
                    "/uploadurlv2" -> testGetUploadUrlV2(client, ws, chatId)
                    "/downloadurlv2" -> testGetDownloadUrlV2(client, ws, chatId, arg)

                    // ── Updates ──
                    "/getupdates" -> testGetUpdates(client, ws, chatId)
                    "/getupdatesv2" -> testGetUpdatesV2(client, ws, chatId)
                    "/setupdatesettings" -> testSetUpdateSettings(client, ws, chatId)
                    "/getinvites" -> testGetWorkspaceInvites(client, ws, chatId)
                    "/acceptinvite" -> testAcceptInvite(client, ws, chatId, arg)
                    "/rejectinvites" -> testRejectInvites(client, ws, chatId)

                    // ── Webhooks ──
                    "/setwebhook" -> testSetWebhook(client, ws, chatId, arg)
                    "/deletewebhook" -> testDeleteWebhook(client, ws, chatId)
                    "/getwebhookinfo" -> testGetWebhookInfo(client, ws, chatId)
                    "/setwebhookv2" -> testSetWebhookV2(client, ws, chatId, arg)
                    "/deletewebhookv2" -> testDeleteWebhookV2(client, ws, chatId)
                    "/getwebhookinfov2" -> testGetWebhookInfoV2(client, ws, chatId)

                    // ── Run all safe ──
                    "/testall" -> testAllSafe(client, ws, chatId, msgId)
                    "/testv1" -> testAllV1(client, ws, chatId, msgId)
                    "/testv2" -> testAllV2(client, ws, chatId, msgId)

                    else -> client.messages.send(ws, chatId, "Неизвестная команда: $cmd\nОтправьте /help")
                }
            } catch (e: Exception) {
                val error = "❌ Ошибка ($cmd): ${e::class.simpleName}: ${e.message}"
                System.err.println("═══════════════════════════════════════")
                System.err.println("COMMAND: $cmd ${arg ?: ""}")
                System.err.println("ERROR: ${e::class.qualifiedName}")
                System.err.println("MESSAGE: ${e.message}")
                e.printStackTrace(System.err)
                System.err.println("═══════════════════════════════════════")
                client.messages.send(ws, chatId, error.take(4000))
            }
        }

        onError { e ->
            System.err.println("Polling error: ${e.message}")
        }
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
        polling.stop()
        client.close()
    })

    Thread.currentThread().join()
}

// ═══════════════════════════════════════════════════════════════
// Help
// ═══════════════════════════════════════════════════════════════

suspend fun showHelp(client: YuChatBotClient, ws: String, chatId: String) {
    val help = """
**Тестовый бот — все эндпоинты YuChat API**
        
`/testall` — запуск всех безопасных тестов (v1 + v2)
`/testv1` — тесты только V1 API
`/testv2` — тесты только V2 API

**Bot:**
`/getme` — информация о боте (v2)

**Messages v1:**
`/send` — отправка сообщения
`/edit` — отправка + редактирование
`/delete` — отправка + удаление
`/forward <targetChatId>` — пересылка

**Messages v2:**
`/sendv2` — отправка с кнопками
`/editv2` — отправка + редактирование
`/deletev2` — отправка + удаление
`/forwardv2 <targetChatId>` — пересылка
`/getmessages` — получение сообщений
`/getmessagebyid` — получение по ID
`/pin <messageId>` — закрепить
`/unpin <messageId>` — открепить
`/reaction [emoji]` — реакция на команду

**Chats v1:**
`/createworkspace [name]` — создать чат
`/createpersonal <participant>` — личный чат
`/createthread` — создать тред
`/listworkspace` — список чатов
`/invite <memberId>` — пригласить
`/kick <memberId>` — исключить

**Chats v2:**
`/getworkspacechats` — чаты воркспейса
`/getmychats` — мои чаты
`/getchatsinfo` — инфо о текущем чате
`/leavechat <chatId>` — покинуть чат
`/archivechat <chatId>` — архивировать
`/unarchivechat <chatId>` — разархивировать
`/setmemberrole <membershipId> <ADMIN|REGULAR>` — роль в чате
`/invitev2 <membershipId>` — пригласить (v2)
`/kickv2 <membershipId>` — исключить (v2)
`/eventschat` — чат событий
`/createworkspacev2 <name>` — создать чат (v2)
`/personalbychat <participant>` — личный чат (v2)
`/threadv2 <parentChatId> <parentMsgId>` — тред (v2)

**Members:**
`/memberlist` — список (v1)
`/getmembers` — список (v2)
`/invitemember <email>` — пригласить
`/removemember <membershipId>` — удалить
`/setrole <membershipId> <ADMIN|REGULAR|OWNER>` — роль
`/getmembersinfo <membershipId>` — инфо

**Files:**
`/uploadurl` — URL загрузки (v1)
`/downloadurl <fileId>` — URL скачивания (v1)
`/uploadurlv2` — URL загрузки (v2)
`/downloadurlv2 <fileId>` — URL скачивания (v2)

**Updates:**
`/getupdates` — обновления (v1)
`/getupdatesv2` — обновления (v2)
`/setupdatesettings` — настройка обновлений
`/getinvites` — приглашения в воркспейсы
`/acceptinvite <workspaceId>` — принять
`/rejectinvites` — отклонить все

**Webhooks:**
`/setwebhook <url>` — установить (v1)
`/deletewebhook` — удалить (v1)
`/getwebhookinfo` — инфо (v1)
`/setwebhookv2 <url>` — установить (v2)
`/deletewebhookv2` — удалить (v2)
`/getwebhookinfov2` — инфо (v2)
    """.trimIndent()
    client.messages.send(ws, chatId, help)
}

// ═══════════════════════════════════════════════════════════════
// Bot
// ═══════════════════════════════════════════════════════════════

suspend fun testGetMe(client: YuChatBotClient, ws: String, chatId: String) {
    val me = client.bot.getMe()
    client.messages.send(ws, chatId, """
✅ **getMe** — OK
- profile: ${me.profile.fullName ?: me.profile.email ?: me.profile.accountId} (${me.profile.accountType})
- workspaces: ${me.workspaces.size}
- scope: ${me.scope.type.name}
    """.trimIndent())
}

// ═══════════════════════════════════════════════════════════════
// Messages v1
// ═══════════════════════════════════════════════════════════════

suspend fun testSend(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.messages.send(ws, chatId, "✅ **send (v1)** — тестовое сообщение")
    client.messages.send(ws, chatId, "✅ **send** — OK, messageId: `${resp.messageId}`")
}

suspend fun testEdit(client: YuChatBotClient, ws: String, chatId: String) {
    val sent = client.messages.send(ws, chatId, "⏳ Это сообщение будет отредактировано...")
    val resp = client.messages.edit(ws, chatId, sent.messageId, "✅ **edit (v1)** — сообщение отредактировано!")
    client.messages.send(ws, chatId, "✅ **edit** — OK, updatedAt: `${resp.updatedAt}`")
}

suspend fun testDelete(client: YuChatBotClient, ws: String, chatId: String) {
    val sent = client.messages.send(ws, chatId, "⏳ Это сообщение будет удалено...")
    val resp = client.messages.delete(ws, chatId, sent.messageId)
    client.messages.send(ws, chatId, "✅ **delete (v1)** — OK, updatedAt: `${resp.updatedAt}`")
}

suspend fun testForward(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val targetChatId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите targetChatId: `/forward <chatId>`")
        return
    }
    val sent = client.messages.send(ws, chatId, "✅ Это сообщение будет переслано")
    val resp = client.messages.forward(ws, chatId, sent.messageId, targetChatId, "Пересланное сообщение")
    client.messages.send(ws, chatId, "✅ **forward (v1)** — OK, messageId: `${resp.messageId}`")
}

// ═══════════════════════════════════════════════════════════════
// Messages v2
// ═══════════════════════════════════════════════════════════════

suspend fun testSendV2(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.messages.sendV2(
        ws, chatId,
        text = "✅ **sendV2** — сообщение с кнопками",
        buttonBar = ButtonBar(
            buttonGroups = listOf(
                ButtonGroup(buttons = listOf(
                    Button(commandButton = CommandButton("Кнопка 1", "btn_1")),
                    Button(commandButton = CommandButton("Кнопка 2", "btn_2"))
                ))
            )
        )
    )
    client.messages.send(ws, chatId, "✅ **sendV2** — OK, messageId: `${resp.messageId}`")
}

suspend fun testEditV2(client: YuChatBotClient, ws: String, chatId: String) {
    val sent = client.messages.sendV2(ws, chatId, text = "⏳ Это сообщение будет отредактировано (v2)...")
    client.messages.editV2(ws, chatId, sent.messageId, text = "✅ **editV2** — сообщение отредактировано!")
    client.messages.send(ws, chatId, "✅ **editV2** — OK")
}

suspend fun testDeleteV2(client: YuChatBotClient, ws: String, chatId: String) {
    val sent = client.messages.sendV2(ws, chatId, text = "⏳ Это сообщение будет удалено (v2)...")
    val resp = client.messages.deleteV2(ws, chatId, listOf(sent.messageId))
    client.messages.send(ws, chatId, "✅ **deleteV2** — OK, deletedCount: `${resp.deletedMessageIds.size}`")
}

suspend fun testForwardV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val targetChatId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите targetChatId: `/forwardv2 <chatId>`")
        return
    }
    val sent = client.messages.sendV2(ws, chatId, text = "✅ Это сообщение будет переслано (v2)")
    val resp = client.messages.forwardV2(ws, chatId, targetChatId, listOf(sent.messageId), "Пересланное v2")
    client.messages.send(ws, chatId, "✅ **forwardV2** — OK, messageId: `${resp.messageId}`")
}

suspend fun testGetMessages(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.messages.getMessages(ws, chatId, pageSize = 5)
    val count = resp.messages.size
    client.messages.send(ws, chatId, "✅ **getMessages** — OK, получено $count сообщений")
}

suspend fun testGetMessageById(client: YuChatBotClient, ws: String, chatId: String, msgId: String) {
    val msg = client.messages.getById(ws, chatId, msgId)
    client.messages.send(ws, chatId, "✅ **getMessageById** — OK, membershipId: `${msg.membershipId}`, text: `${msg.content.text ?: "(empty)"}`")
}

suspend fun testPin(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val messageId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите messageId: `/pin <messageId>`")
        return
    }
    client.messages.pin(ws, chatId, messageId)
    client.messages.send(ws, chatId, "✅ **pin** — OK, закреплено: `$messageId`")
}

suspend fun testUnpin(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val messageId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите messageId: `/unpin <messageId>`")
        return
    }
    client.messages.unpin(ws, chatId, messageId)
    client.messages.send(ws, chatId, "✅ **unpin** — OK, откреплено: `$messageId`")
}

suspend fun testReaction(client: YuChatBotClient, ws: String, chatId: String, msgId: String, arg: String?) {
    val emoji = arg ?: "👍"
    val resp = client.messages.toggleReaction(ws, chatId, msgId, emoji)
    client.messages.send(ws, chatId, "✅ **toggleReaction** — OK, reactionAdded: `${resp.reactionAdded}`, emoji: $emoji")
}

// ═══════════════════════════════════════════════════════════════
// Chats v1
// ═══════════════════════════════════════════════════════════════

suspend fun testCreateWorkspaceChat(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val name = arg ?: "TestBot Chat ${System.currentTimeMillis() % 10000}"
    val resp = client.chats.createWorkspace(ws, name = name, type = WorkspaceChatType.PUBLIC)
    client.messages.send(ws, chatId, "✅ **createWorkspace (v1)** — OK, chatId: `${resp.chatId}`")
}

suspend fun testCreatePersonalChat(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val participant = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите participant: `/createpersonal <userId>`")
        return
    }
    val resp = client.chats.createPersonal(ws, participant)
    client.messages.send(ws, chatId, "✅ **createPersonal (v1)** — OK, chatId: `${resp.chatId}`")
}

suspend fun testCreateThread(client: YuChatBotClient, ws: String, chatId: String, msgId: String) {
    val resp = client.chats.createThread(ws, chatId, msgId)
    client.messages.send(ws, chatId, "✅ **createThread (v1)** — OK, chatId: `${resp.chatId}`")
}

suspend fun testListWorkspace(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.chats.listWorkspace(ws, maxCount = 5)
    val count = resp.workspaceChats?.size ?: 0
    client.messages.send(ws, chatId, "✅ **listWorkspace (v1)** — OK, чатов: $count")
}

suspend fun testInviteV1(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val memberId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите memberId: `/invite <memberId>`")
        return
    }
    val resp = client.chats.invite(ws, chatId, listOf(memberId))
    client.messages.send(ws, chatId, "✅ **invite (v1)** — OK, chat: `${resp.chat?.chatId}`")
}

suspend fun testKickV1(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val memberId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите memberId: `/kick <memberId>`")
        return
    }
    val resp = client.chats.kick(ws, chatId, listOf(memberId))
    client.messages.send(ws, chatId, "✅ **kick (v1)** — OK, chat: `${resp.chat?.chatId}`")
}

// ═══════════════════════════════════════════════════════════════
// Chats v2
// ═══════════════════════════════════════════════════════════════

suspend fun testGetWorkspaceChats(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.chats.getWorkspaceChats(ws, pageSize = 5)
    client.messages.send(ws, chatId, "✅ **getWorkspaceChats** — OK, chatIds: ${resp.chatIds.size}")
}

suspend fun testGetMyChats(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.chats.getMyChats(ws, pageSize = 5)
    client.messages.send(ws, chatId, "✅ **getMyChats** — OK, chatIds: ${resp.chatIds.size}")
}

suspend fun testGetChatsInfo(client: YuChatBotClient, ws: String, chatId: String) {
    val info = client.chats.getInfo(ws, listOf(chatId))
    val first = info.firstOrNull()
    client.messages.send(ws, chatId, "✅ **getChatsInfo** — OK, чатов: ${info.size}, первый: `${first?.chatId}`")
}

suspend fun testLeaveChat(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val targetChat = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите chatId: `/leavechat <chatId>` (⚠️ бот покинет чат!)")
        return
    }
    client.chats.leave(ws, targetChat)
    client.messages.send(ws, chatId, "✅ **leaveChat** — OK, покинут чат: `$targetChat`")
}

suspend fun testArchiveChat(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val targetChat = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите chatId: `/archivechat <chatId>`")
        return
    }
    client.chats.archive(ws, targetChat)
    client.messages.send(ws, chatId, "✅ **archiveChat** — OK, архивирован: `$targetChat`")
}

suspend fun testUnarchiveChat(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val targetChat = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите chatId: `/unarchivechat <chatId>`")
        return
    }
    client.chats.unarchive(ws, targetChat)
    client.messages.send(ws, chatId, "✅ **unarchiveChat** — OK, разархивирован: `$targetChat`")
}

suspend fun testSetChatMemberRole(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val parts = arg?.split(" ") ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/setmemberrole <membershipId> <ADMIN|REGULAR>`")
        return
    }
    if (parts.size < 2) {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/setmemberrole <membershipId> <ADMIN|REGULAR>`")
        return
    }
    val role = ChatRole.valueOf(parts[1].uppercase())
    client.chats.setMemberRole(ws, chatId, parts[0], role)
    client.messages.send(ws, chatId, "✅ **setChatMemberRole** — OK, ${parts[0]} → $role")
}

suspend fun testInviteV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val membershipId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/invitev2 <membershipId>`")
        return
    }
    client.chats.inviteV2(ws, chatId, listOf(membershipId))
    client.messages.send(ws, chatId, "✅ **inviteToChat (v2)** — OK")
}

suspend fun testKickV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val membershipId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/kickv2 <membershipId>`")
        return
    }
    client.chats.kickV2(ws, chatId, listOf(membershipId))
    client.messages.send(ws, chatId, "✅ **kickFromChat (v2)** — OK")
}

suspend fun testCreateEventsChat(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.chats.createUserEventsChat(ws)
    client.messages.send(ws, chatId, "✅ **getOrCreateUserEventsChat** — OK, chatId: `${resp.chatId}`")
}

suspend fun testCreateWorkspaceChatV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val name = arg ?: "TestV2 Chat ${System.currentTimeMillis() % 10000}"
    val resp = client.chats.createWorkspaceChatV2(ws, name, WorkspaceChatType.PUBLIC)
    client.messages.send(ws, chatId, "✅ **createWorkspaceChat (v2)** — OK, chatId: `${resp.chatId}`")
}

suspend fun testGetOrCreatePersonal(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val participant = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/personalbychat <participant>`")
        return
    }
    val resp = client.chats.getOrCreatePersonalChat(ws, participant)
    client.messages.send(ws, chatId, "✅ **getOrCreatePersonalChat** — OK, chatId: `${resp.chatId}`")
}

suspend fun testGetOrCreateThread(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val parts = arg?.split(" ") ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/threadv2 <parentChatId> <parentMsgId>`")
        return
    }
    if (parts.size < 2) {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/threadv2 <parentChatId> <parentMsgId>`")
        return
    }
    val resp = client.chats.getOrCreateThreadChat(ws, parts[0], parts[1])
    client.messages.send(ws, chatId, "✅ **getOrCreateThreadChat** — OK, chatId: `${resp.chatId}`")
}

// ═══════════════════════════════════════════════════════════════
// Members
// ═══════════════════════════════════════════════════════════════

suspend fun testMemberList(client: YuChatBotClient, ws: String, chatId: String) {
    val members = client.members.list(ws)
    client.messages.send(ws, chatId, "✅ **member.list (v1)** — OK, участников: ${members.size}")
}

suspend fun testGetMembers(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.members.getMembers(ws, pageSize = 5)
    client.messages.send(ws, chatId, "✅ **getMembers (v2)** — OK, membershipIds: ${resp.membershipIds.size}")
}

suspend fun testInviteMember(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val email = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите email: `/invitemember <email>`")
        return
    }
    client.members.invite(ws, listOf(email))
    client.messages.send(ws, chatId, "✅ **inviteMember (v2)** — OK, приглашён: `$email`")
}

suspend fun testRemoveMember(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val membershipId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/removemember <membershipId>`")
        return
    }
    client.members.remove(ws, membershipId)
    client.messages.send(ws, chatId, "✅ **removeMember (v2)** — OK, удалён: `$membershipId`")
}

suspend fun testSetRole(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val parts = arg?.split(" ") ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/setrole <membershipId> <ADMIN|REGULAR|OWNER>`")
        return
    }
    if (parts.size < 2) {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/setrole <membershipId> <ADMIN|REGULAR|OWNER>`")
        return
    }
    val role = WorkspaceRole.valueOf(parts[1].uppercase())
    client.members.setRole(ws, parts[0], role)
    client.messages.send(ws, chatId, "✅ **setWorkspaceMemberRole (v2)** — OK, ${parts[0]} → $role")
}

suspend fun testGetMembersInfo(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val membershipId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/getmembersinfo <membershipId>`")
        return
    }
    val info = client.members.getInfo(ws, listOf(membershipId))
    val first = info.firstOrNull()
    client.messages.send(ws, chatId, "✅ **getMembersInfo (v2)** — OK, members: ${info.size}, name: `${first?.profile?.fullName}`")
}

// ═══════════════════════════════════════════════════════════════
// Files
// ═══════════════════════════════════════════════════════════════

suspend fun testGetUploadUrl(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.files.getUploadUrl(ws, "test.txt", MediaType.RAW)
    client.messages.send(ws, chatId, "✅ **getUploadUrl (v1)** — OK, fileId: `${resp.fileId}`")
}

suspend fun testGetDownloadUrl(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val fileId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите fileId: `/downloadurl <fileId>`")
        return
    }
    val resp = client.files.getDownloadUrl(fileId)
    client.messages.send(ws, chatId, "✅ **getDownloadUrl (v1)** — OK, url: `${resp.url.take(50)}...`")
}

suspend fun testGetUploadUrlV2(client: YuChatBotClient, ws: String, chatId: String) {
    val resp = client.files.getUploadUrlV2(ws, "test_v2.txt")
    client.messages.send(ws, chatId, "✅ **getFileUploadUrl (v2)** — OK, fileId: `${resp.fileId}`")
}

suspend fun testGetDownloadUrlV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val fileId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите fileId: `/downloadurlv2 <fileId>`")
        return
    }
    val resp = client.files.getDownloadUrlV2(fileId)
    client.messages.send(ws, chatId, "✅ **getFileDownloadUrl (v2)** — OK, url: `${resp.url.take(50)}...`")
}

// ═══════════════════════════════════════════════════════════════
// Updates
// ═══════════════════════════════════════════════════════════════

suspend fun testGetUpdates(client: YuChatBotClient, ws: String, chatId: String) {
    val updates = client.updates.getUpdates(limit = 1)
    client.messages.send(ws, chatId, "✅ **getUpdates (v1)** — OK, обновлений: ${updates.size}")
}

suspend fun testGetUpdatesV2(client: YuChatBotClient, ws: String, chatId: String) {
    val updates = client.updates.getUpdatesV2(limit = 1)
    client.messages.send(ws, chatId, "✅ **getUpdates (v2)** — OK, обновлений: ${updates.size}")
}

suspend fun testSetUpdateSettings(client: YuChatBotClient, ws: String, chatId: String) {
    client.updates.setUpdateSettings(
        updateSettings = listOf(
            UpdateSetting.MESSAGE
        ),
        updateApiVersion = 1,
        autoAcceptWorkspaceInvites = false
    )
    client.messages.send(ws, chatId, "✅ **setUpdateSettings** — OK")
}

suspend fun testGetWorkspaceInvites(client: YuChatBotClient, ws: String, chatId: String) {
    val invites = client.updates.getWorkspaceInvites()
    client.messages.send(ws, chatId, "✅ **getMyWorkspaceInvites** — OK, приглашений: ${invites.size}")
}

suspend fun testAcceptInvite(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val workspaceId = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите: `/acceptinvite <workspaceId>`")
        return
    }
    client.updates.acceptWorkspaceInvite(workspaceId)
    client.messages.send(ws, chatId, "✅ **acceptWorkspaceInvite** — OK")
}

suspend fun testRejectInvites(client: YuChatBotClient, ws: String, chatId: String) {
    client.updates.rejectAllWorkspaceInvites()
    client.messages.send(ws, chatId, "✅ **rejectAllWorkspaceInvites** — OK")
}

// ═══════════════════════════════════════════════════════════════
// Webhooks
// ═══════════════════════════════════════════════════════════════

suspend fun testSetWebhook(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val url = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите URL: `/setwebhook <url>`")
        return
    }
    client.webhooks.setWebhook(SetWebhookRequest(url = url))
    client.messages.send(ws, chatId, "✅ **setWebhook (v1)** — OK")
}

suspend fun testDeleteWebhook(client: YuChatBotClient, ws: String, chatId: String) {
    client.webhooks.deleteWebhook()
    client.messages.send(ws, chatId, "✅ **deleteWebhook (v1)** — OK")
}

suspend fun testGetWebhookInfo(client: YuChatBotClient, ws: String, chatId: String) {
    val info = client.webhooks.getWebhookInfo()
    client.messages.send(ws, chatId, "✅ **getWebhookInfo (v1)** — OK, url: `${info.url.ifBlank { "(не установлен)" }}`")
}

suspend fun testSetWebhookV2(client: YuChatBotClient, ws: String, chatId: String, arg: String?) {
    val url = arg ?: run {
        client.messages.send(ws, chatId, "⚠️ Укажите URL: `/setwebhookv2 <url>`")
        return
    }
    client.webhooks.setWebhookV2(SetWebhookRequest(url = url))
    client.messages.send(ws, chatId, "✅ **setWebhook (v2)** — OK")
}

suspend fun testDeleteWebhookV2(client: YuChatBotClient, ws: String, chatId: String) {
    client.webhooks.deleteWebhookV2()
    client.messages.send(ws, chatId, "✅ **deleteWebhook (v2)** — OK")
}

suspend fun testGetWebhookInfoV2(client: YuChatBotClient, ws: String, chatId: String) {
    val info = client.webhooks.getWebhookInfoV2()
    client.messages.send(ws, chatId, "✅ **getWebhookInfo (v2)** — OK, url: `${info.url.ifBlank { "(не установлен)" }}`")
}

// ═══════════════════════════════════════════════════════════════
// Test All (safe endpoints only)
// ═══════════════════════════════════════════════════════════════

suspend fun testAllSafe(client: YuChatBotClient, ws: String, chatId: String, msgId: String) {
    client.messages.send(ws, chatId, "🚀 **Запуск тестов всех безопасных эндпоинтов (V1 + V2)...**")
    val results = mutableListOf<String>()
    runV1Tests(client, ws, chatId, msgId, results)
    runV2Tests(client, ws, chatId, msgId, results)
    sendReport("V1 + V2", client, ws, chatId, results)
}

suspend fun testAllV1(client: YuChatBotClient, ws: String, chatId: String, msgId: String) {
    client.messages.send(ws, chatId, "🚀 **Запуск тестов V1 API...**")
    val results = mutableListOf<String>()
    runV1Tests(client, ws, chatId, msgId, results)
    sendReport("V1", client, ws, chatId, results)
}

suspend fun testAllV2(client: YuChatBotClient, ws: String, chatId: String, msgId: String) {
    client.messages.send(ws, chatId, "🚀 **Запуск тестов V2 API...**")
    val results = mutableListOf<String>()
    runV2Tests(client, ws, chatId, msgId, results)
    sendReport("V2", client, ws, chatId, results)
}

private suspend fun runTest(name: String, results: MutableList<String>, block: suspend () -> Unit) {
    try {
        block()
        results.add("✅ $name")
        println("  ✅ $name")
    } catch (e: Exception) {
        val msg = e.message?.take(120) ?: "unknown"
        results.add("❌ $name — ${e::class.simpleName}: $msg")
        System.err.println("  ❌ $name")
        System.err.println("     ${e::class.qualifiedName}: ${e.message}")
        e.cause?.let { System.err.println("     Caused by: ${it::class.qualifiedName}: ${it.message}") }
    }
}

private suspend fun runV1Tests(
    client: YuChatBotClient, ws: String, chatId: String, msgId: String,
    results: MutableList<String>
) {
    println("── V1 Tests ──")

    // Messages v1
    runTest("send (v1)", results) {
        client.messages.send(ws, chatId, "⏳ testAll: send v1")
    }

    runTest("send+edit (v1)", results) {
        val r = client.messages.send(ws, chatId, "⏳ будет отредактировано")
        client.messages.edit(ws, chatId, r.messageId, "✅ отредактировано (v1)")
    }

    runTest("send+delete (v1)", results) {
        val r = client.messages.send(ws, chatId, "⏳ будет удалено")
        client.messages.delete(ws, chatId, r.messageId)
    }

    // Chats v1
    runTest("listWorkspace (v1)", results) {
        val r = client.chats.listWorkspace(ws, maxCount = 3)
        println("     chats: ${r.workspaceChats?.size ?: 0}")
    }

    // Members v1
    runTest("member.list (v1)", results) {
        val r = client.members.list(ws)
        println("     members: ${r.size}")
    }

    // Files v1
    runTest("getUploadUrl (v1)", results) {
        val r = client.files.getUploadUrl(ws, "test.txt", MediaType.RAW)
        println("     fileId: ${r.fileId}")
    }

    // Updates v1
    runTest("getUpdates (v1)", results) {
        val r = client.updates.getUpdates(limit = 1)
        println("     updates: ${r.size}")
    }

    // Webhooks v1
    runTest("getWebhookInfo (v1)", results) {
        val r = client.webhooks.getWebhookInfo()
        println("     url: ${r.url}")
    }
}

private suspend fun runV2Tests(
    client: YuChatBotClient, ws: String, chatId: String, msgId: String,
    results: MutableList<String>
) {
    println("── V2 Tests ──")

    // Bot (v2)
    runTest("getMe (v2)", results) {
        val r = client.bot.getMe()
        println("     accountId: ${r.profile.accountId}, workspaces: ${r.workspaces.size}")
    }

    // Messages v2
    runTest("sendV2", results) {
        val r = client.messages.sendV2(ws, chatId, text = "⏳ testAll: send v2")
        println("     messageId: ${r.messageId}")
    }

    runTest("sendV2+editV2", results) {
        val r = client.messages.sendV2(ws, chatId, text = "⏳ будет отредактировано (v2)")
        client.messages.editV2(ws, chatId, r.messageId, text = "✅ отредактировано (v2)")
    }

    runTest("sendV2+deleteV2", results) {
        val r = client.messages.sendV2(ws, chatId, text = "⏳ будет удалено (v2)")
        client.messages.deleteV2(ws, chatId, listOf(r.messageId))
    }

    runTest("getMessages (v2)", results) {
        val r = client.messages.getMessages(ws, chatId, pageSize = 3)
        println("     messages: ${r.messages.size}")
    }

    runTest("getMessageById (v2)", results) {
        val r = client.messages.getById(ws, chatId, msgId)
        println("     messageId: ${r.messageId}")
    }

    runTest("toggleReaction (v2)", results) {
        val r = client.messages.toggleReaction(ws, chatId, msgId, "👍")
        println("     reactionAdded: ${r.reactionAdded}")
        // Undo
        try { client.messages.toggleReaction(ws, chatId, msgId, "👍") } catch (_: Exception) {}
    }

    // Chats v2
    runTest("getWorkspaceChats (v2)", results) {
        val r = client.chats.getWorkspaceChats(ws, pageSize = 3)
        println("     chatIds: ${r.chatIds.size}")
    }

    runTest("getMyChats (v2)", results) {
        val r = client.chats.getMyChats(ws, pageSize = 3)
        println("     chatIds: ${r.chatIds.size}")
    }

    runTest("getChatsInfo (v2)", results) {
        val r = client.chats.getInfo(ws, listOf(chatId))
        println("     chats: ${r.size}")
    }

    runTest("getOrCreateUserEventsChat (v2)", results) {
        val r = client.chats.createUserEventsChat(ws)
        println("     chatId: ${r.chatId}")
    }

    // Members v2
    runTest("getMembers (v2)", results) {
        val r = client.members.getMembers(ws, pageSize = 3)
        println("     membershipIds: ${r.membershipIds.size}")
    }

    // Files v2
    runTest("getUploadUrlV2", results) {
        val r = client.files.getUploadUrlV2(ws, "test.txt")
        println("     fileId: ${r.fileId}")
    }

    // Updates v2
    runTest("getWorkspaceInvites (v2)", results) {
        val r = client.updates.getWorkspaceInvites()
        println("     invites: ${r.size}")
    }

    // Webhooks v2
    runTest("getWebhookInfoV2", results) {
        val r = client.webhooks.getWebhookInfoV2()
        println("     url: ${r.url}")
    }
}

private suspend fun sendReport(
    label: String, client: YuChatBotClient, ws: String, chatId: String,
    results: List<String>
) {
    val passed = results.count { it.startsWith("✅") }
    val failed = results.count { it.startsWith("❌") }

    val report = buildString {
        appendLine("📊 **Результаты тестирования ($label)**")
        appendLine("Пройдено: **$passed** | Ошибок: **$failed**")
        appendLine()
        results.forEach { appendLine(it) }
    }

    // Отправляем в чат (с ограничением длины)
    if (report.length <= 4000) {
        client.messages.send(ws, chatId, report)
    } else {
        // Разбиваем на части
        val header = "📊 **Результаты тестирования ($label)** — Пройдено: **$passed** | Ошибок: **$failed**\n\n"
        client.messages.send(ws, chatId, header)
        val chunk = StringBuilder()
        for (line in results) {
            if (chunk.length + line.length + 1 > 3800) {
                client.messages.send(ws, chatId, chunk.toString())
                chunk.clear()
            }
            chunk.appendLine(line)
        }
        if (chunk.isNotEmpty()) {
            client.messages.send(ws, chatId, chunk.toString())
        }
    }

    // Всегда дублируем в консоль полностью
    println("\n$report")
}
