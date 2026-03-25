package ru.rt.yuchatbotapi.api

import kotlinx.serialization.Serializable
import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*

/**
 * Работа с чатами.
 *
 * Базовые методы ([createWorkspace], [createPersonal], [createThread], [listWorkspace],
 * [invite], [kick]) используют API v1.
 * Расширенные ([getWorkspaceChats], [getMyChats], [getInfo], [leave], [archive],
 * [unarchive], [setMemberRole], [inviteV2], [kickV2], [createUserEventsChat]) используют API v2.
 */
class ChatsApi internal constructor(private val client: YuChatHttpClient) {

    // ── v1 ──

    /** Создание воркспейс-чата (v1) */
    suspend fun createWorkspace(
        workspaceId: String,
        name: String? = null,
        type: WorkspaceChatType? = null,
        participants: List<String>? = null,
        announce: Boolean? = null,
        description: String? = null
    ): CreateChatResponse {
        return client.post("/public/v1/chat.workspace.create", CreateWorkspaceChatV1Request(
            workspaceId = workspaceId,
            name = name,
            type = type,
            participants = participants,
            announceChannel = announce,
            description = description
        ))
    }

    /** Создание личного чата (v1) */
    suspend fun createPersonal(workspaceId: String, participant: String): CreateChatResponse {
        return client.post("/public/v1/chat.personal.create", CreatePersonalChatV1Request(
            workspaceId = workspaceId,
            participant = participant
        ))
    }

    /** Создание треда (v1) */
    suspend fun createThread(
        workspaceId: String,
        chatId: String,
        parentMessageId: String
    ): CreateChatResponse {
        return client.post("/public/v1/chat.thread.create", CreateThreadChatV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            parentMessageId = parentMessageId
        ))
    }

    /** Список чатов воркспейса (v1) */
    suspend fun listWorkspace(
        workspaceId: String,
        chatIds: List<String>? = null,
        maxCount: Int? = null
    ): ListWorkspaceChatsResponse {
        return client.post("/public/v1/chat.workspace.list", ListWorkspaceChatsV1Request(
            workspaceId = workspaceId,
            chatIds = chatIds,
            maxCount = maxCount
        ))
    }

    /** Приглашение участников в чат (v1) */
    suspend fun invite(
        workspaceId: String,
        chatId: String,
        memberIds: List<String>
    ): InviteToChatResponse {
        return client.post("/public/v1/chat.invite", InviteToChatV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            memberId = memberIds
        ))
    }

    /** Исключение участников из чата (v1) */
    suspend fun kick(
        workspaceId: String,
        chatId: String,
        memberIds: List<String>
    ): KickFromChatResponse {
        return client.post("/public/v1/chat.kick", KickFromChatV1Request(
            workspaceId = workspaceId,
            chatId = chatId,
            memberId = memberIds
        ))
    }

    // ── v2 only ──

    /** Получение списка чатов воркспейса (v2) */
    suspend fun getWorkspaceChats(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetChatsResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getWorkspaceChats", Req(workspaceId, pageSize, pageToken))
    }

    /** Получение своих чатов в воркспейсе (v2) */
    suspend fun getMyChats(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetChatsResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getMyWorkspaceChats", Req(workspaceId, pageSize, pageToken))
    }

    /** Информация о чатах (v2) */
    suspend fun getInfo(workspaceId: String, chatIds: List<String>): List<ChatMembership> {
        @Serializable
        data class Req(val workspaceId: String, val chatIds: List<String>)
        @Serializable
        data class Resp(val chatMemberships: List<ChatMembership>)
        return client.post<Resp>("/public/v2/getChatsInfo", Req(workspaceId, chatIds)).chatMemberships
    }

    /** Выход из чата (v2) */
    suspend fun leave(workspaceId: String, chatId: String) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/leaveChat", Req(workspaceId, chatId))
    }

    /** Архивация чата (v2) */
    suspend fun archive(workspaceId: String, chatId: String) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/archiveChat", Req(workspaceId, chatId))
    }

    /** Разархивация чата (v2) */
    suspend fun unarchive(workspaceId: String, chatId: String) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/unrchiveChat", Req(workspaceId, chatId))
    }

    /** Изменение роли участника в чате (v2) */
    suspend fun setMemberRole(workspaceId: String, chatId: String, membershipId: String, role: ChatRole) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipId: String, val role: ChatRole)
        client.postNoContent("/public/v2/setChatMemberRole", Req(workspaceId, chatId, membershipId, role))
    }

    /** Приглашение в чат (v2) */
    suspend fun inviteV2(workspaceId: String, chatId: String, membershipIds: List<String>) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipIds: List<String>)
        client.postNoContent("/public/v2/inviteToChat", Req(workspaceId, chatId, membershipIds))
    }

    /** Исключение из чата (v2) */
    suspend fun kickV2(workspaceId: String, chatId: String, membershipIds: List<String>) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipIds: List<String>)
        client.postNoContent("/public/v2/kickFromChat", Req(workspaceId, chatId, membershipIds))
    }

    /** Создание чата событий (v2) */
    suspend fun createUserEventsChat(workspaceId: String, eventsType: EventsType? = null): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val eventsType: EventsType? = null)
        return client.post("/public/v2/getOrCreateUserEventsChat", Req(workspaceId, eventsType))
    }

    /** Создание воркспейс-чата (v2) */
    suspend fun createWorkspaceChatV2(
        workspaceId: String,
        name: String,
        workspaceChatType: WorkspaceChatType,
        participants: List<String>? = null,
        announce: Boolean? = null,
        autoJoinNewMembers: Boolean? = null,
        description: String? = null
    ): CreateChatResponse {
        @Serializable
        data class Req(
            val workspaceId: String,
            val name: String,
            val workspaceChatType: WorkspaceChatType,
            val participants: List<String>? = null,
            val announce: Boolean? = null,
            val autoJoinNewMembers: Boolean? = null,
            val description: String? = null
        )
        return client.post("/public/v2/createWorkspaceChat", Req(
            workspaceId, name, workspaceChatType, participants, announce, autoJoinNewMembers, description
        ))
    }

    /** Создание или получение личного чата (v2) */
    suspend fun getOrCreatePersonalChat(workspaceId: String, participant: String): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val participant: String)
        return client.post("/public/v2/getOrCreatePersonalChat", Req(workspaceId, participant))
    }

    /** Создание или получение треда (v2) */
    suspend fun getOrCreateThreadChat(
        workspaceId: String,
        parentChatId: String,
        parentMessageId: String
    ): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val parentChatId: String, val parentMessageId: String)
        return client.post("/public/v2/getOrCreateThreadChat", Req(workspaceId, parentChatId, parentMessageId))
    }
}

// ── Request/Response DTOs ──

@Serializable
internal data class CreateWorkspaceChatV1Request(
    val workspaceId: String,
    val name: String? = null,
    val type: WorkspaceChatType? = null,
    val participants: List<String>? = null,
    val announceChannel: Boolean? = null,
    val description: String? = null
)

@Serializable
internal data class CreatePersonalChatV1Request(
    val workspaceId: String,
    val participant: String
)

@Serializable
internal data class CreateThreadChatV1Request(
    val workspaceId: String,
    val chatId: String,
    val parentMessageId: String
)

/** Ответ на создание чата. */
@Serializable
data class CreateChatResponse(val chatId: String? = null)

@Serializable
internal data class ListWorkspaceChatsV1Request(
    val workspaceId: String,
    val chatIds: List<String>? = null,
    val maxCount: Int? = null
)

/** Ответ со списком воркспейс-чатов (v1). */
@Serializable
data class ListWorkspaceChatsResponse(
    val workspaceChats: List<WorkspaceChatMembership>? = null
)

@Serializable
internal data class InviteToChatV1Request(
    val workspaceId: String,
    val chatId: String,
    val memberId: List<String>
)

/** Ответ на приглашение в чат (v1). */
@Serializable
data class InviteToChatResponse(val chat: WorkspaceChat? = null)

@Serializable
internal data class KickFromChatV1Request(
    val workspaceId: String,
    val chatId: String,
    val memberId: List<String>
)

/** Ответ на исключение из чата (v1). */
@Serializable
data class KickFromChatResponse(val chat: WorkspaceChat? = null)
