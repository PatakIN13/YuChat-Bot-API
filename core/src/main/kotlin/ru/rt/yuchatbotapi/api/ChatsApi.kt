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
        workspaceId: WorkspaceId,
        name: String? = null,
        type: WorkspaceChatType? = null,
        participants: List<MembershipId>? = null,
        announce: Boolean? = null,
        description: String? = null
    ): CreateChatResponse {
        return client.post("/public/v1/chat.workspace.create", CreateWorkspaceChatV1Request(
            workspaceId = workspaceId.value,
            name = name,
            type = type,
            participants = participants?.map { it.value },
            announceChannel = announce,
            description = description
        ))
    }

    /** Создание личного чата (v1) */
    suspend fun createPersonal(workspaceId: WorkspaceId, participant: MembershipId): CreateChatResponse {
        return client.post("/public/v1/chat.personal.create", CreatePersonalChatV1Request(
            workspaceId = workspaceId.value,
            participant = participant.value
        ))
    }

    /** Создание треда (v1) */
    suspend fun createThread(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        parentMessageId: String
    ): CreateChatResponse {
        return client.post("/public/v1/chat.thread.create", CreateThreadChatV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            parentMessageId = parentMessageId
        ))
    }

    /** Список чатов воркспейса (v1) */
    suspend fun listWorkspace(
        workspaceId: WorkspaceId,
        chatIds: List<ChatId>? = null,
        maxCount: Int? = null
    ): ListWorkspaceChatsResponse {
        return client.post("/public/v1/chat.workspace.list", ListWorkspaceChatsV1Request(
            workspaceId = workspaceId.value,
            chatIds = chatIds?.map { it.value },
            maxCount = maxCount
        ))
    }

    /** Приглашение участников в чат (v1) */
    suspend fun invite(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        memberIds: List<MembershipId>
    ): InviteToChatResponse {
        return client.post("/public/v1/chat.invite", InviteToChatV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            memberId = memberIds.map { it.value }
        ))
    }

    /** Исключение участников из чата (v1) */
    suspend fun kick(
        workspaceId: WorkspaceId,
        chatId: ChatId,
        memberIds: List<MembershipId>
    ): KickFromChatResponse {
        return client.post("/public/v1/chat.kick", KickFromChatV1Request(
            workspaceId = workspaceId.value,
            chatId = chatId.value,
            memberId = memberIds.map { it.value }
        ))
    }

    // ── v2 only ──

    /** Получение списка чатов воркспейса (v2) */
    suspend fun getWorkspaceChats(
        workspaceId: WorkspaceId,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetChatsResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getWorkspaceChats", Req(workspaceId.value, pageSize, pageToken))
    }

    /** Получение своих чатов в воркспейсе (v2) */
    suspend fun getMyChats(
        workspaceId: WorkspaceId,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetChatsResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getMyWorkspaceChats", Req(workspaceId.value, pageSize, pageToken))
    }

    /** Информация о чатах (v2) */
    suspend fun getInfo(workspaceId: WorkspaceId, chatIds: List<ChatId>): List<ChatMembership> {
        @Serializable
        data class Req(val workspaceId: String, val chatIds: List<String>)
        @Serializable
        data class Resp(val chatMemberships: List<ChatMembership>)
        return client.post<Resp>("/public/v2/getChatsInfo", Req(workspaceId.value, chatIds.map { it.value })).chatMemberships
    }

    /** Выход из чата (v2) */
    suspend fun leave(workspaceId: WorkspaceId, chatId: ChatId) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/leaveChat", Req(workspaceId.value, chatId.value))
    }

    /** Архивация чата (v2) */
    suspend fun archive(workspaceId: WorkspaceId, chatId: ChatId) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/archiveChat", Req(workspaceId.value, chatId.value))
    }

    /** Разархивация чата (v2) */
    suspend fun unarchive(workspaceId: WorkspaceId, chatId: ChatId) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String)
        client.postNoContent("/public/v2/unrchiveChat", Req(workspaceId.value, chatId.value))
    }

    /** Изменение роли участника в чате (v2) */
    suspend fun setMemberRole(workspaceId: WorkspaceId, chatId: ChatId, membershipId: MembershipId, role: ChatRole) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipId: String, val role: ChatRole)
        client.postNoContent("/public/v2/setChatMemberRole", Req(workspaceId.value, chatId.value, membershipId.value, role))
    }

    /** Приглашение в чат (v2) */
    suspend fun inviteV2(workspaceId: WorkspaceId, chatId: ChatId, membershipIds: List<MembershipId>) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipIds: List<String>)
        client.postNoContent("/public/v2/inviteToChat", Req(workspaceId.value, chatId.value, membershipIds.map { it.value }))
    }

    /** Исключение из чата (v2) */
    suspend fun kickV2(workspaceId: WorkspaceId, chatId: ChatId, membershipIds: List<MembershipId>) {
        @Serializable
        data class Req(val workspaceId: String, val chatId: String, val membershipIds: List<String>)
        client.postNoContent("/public/v2/kickFromChat", Req(workspaceId.value, chatId.value, membershipIds.map { it.value }))
    }

    /** Создание чата событий (v2) */
    suspend fun createUserEventsChat(workspaceId: WorkspaceId, eventsType: EventsType? = null): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val eventsType: EventsType? = null)
        return client.post("/public/v2/getOrCreateUserEventsChat", Req(workspaceId.value, eventsType))
    }

    /** Создание воркспейс-чата (v2) */
    suspend fun createWorkspaceChatV2(
        workspaceId: WorkspaceId,
        name: String,
        workspaceChatType: WorkspaceChatType,
        participants: List<MembershipId>? = null,
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
            workspaceId.value, name, workspaceChatType, participants?.map { it.value }, announce, autoJoinNewMembers, description
        ))
    }

    /** Создание или получение личного чата (v2) */
    suspend fun getOrCreatePersonalChat(workspaceId: WorkspaceId, participant: MembershipId): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val participant: String)
        return client.post("/public/v2/getOrCreatePersonalChat", Req(workspaceId.value, participant.value))
    }

    /** Создание или получение треда (v2) */
    suspend fun getOrCreateThreadChat(
        workspaceId: WorkspaceId,
        parentChatId: ChatId,
        parentMessageId: String
    ): CreateChatResponse {
        @Serializable
        data class Req(val workspaceId: String, val parentChatId: String, val parentMessageId: String)
        return client.post("/public/v2/getOrCreateThreadChat", Req(workspaceId.value, parentChatId.value, parentMessageId))
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
data class CreateChatResponse(@get:JvmName("getChatId") val chatId: ChatId? = null)

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
