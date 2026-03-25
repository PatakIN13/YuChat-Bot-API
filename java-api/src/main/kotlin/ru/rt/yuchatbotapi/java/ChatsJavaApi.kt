package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.*
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.ChatsApi}.
 */
class ChatsJavaApi internal constructor(private val api: ChatsApi) {

    // ── v1 ──

    @JvmOverloads
    fun createWorkspace(
        workspaceId: String,
        name: String? = null,
        type: WorkspaceChatType? = null,
        participants: List<String>? = null,
        announce: Boolean? = null,
        description: String? = null
    ): CompletableFuture<CreateChatResponse> = async {
        api.createWorkspace(workspaceId, name, type, participants, announce, description)
    }

    fun createPersonal(workspaceId: String, participant: String): CompletableFuture<CreateChatResponse> = async {
        api.createPersonal(workspaceId, participant)
    }

    fun createThread(
        workspaceId: String,
        chatId: String,
        parentMessageId: String
    ): CompletableFuture<CreateChatResponse> = async {
        api.createThread(workspaceId, chatId, parentMessageId)
    }

    @JvmOverloads
    fun listWorkspace(
        workspaceId: String,
        chatIds: List<String>? = null,
        maxCount: Int? = null
    ): CompletableFuture<ListWorkspaceChatsResponse> = async {
        api.listWorkspace(workspaceId, chatIds, maxCount)
    }

    fun invite(
        workspaceId: String,
        chatId: String,
        memberIds: List<String>
    ): CompletableFuture<InviteToChatResponse> = async {
        api.invite(workspaceId, chatId, memberIds)
    }

    fun kick(
        workspaceId: String,
        chatId: String,
        memberIds: List<String>
    ): CompletableFuture<KickFromChatResponse> = async {
        api.kick(workspaceId, chatId, memberIds)
    }

    // ── v2 ──

    @JvmOverloads
    fun getWorkspaceChats(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): CompletableFuture<GetChatsResponse> = async {
        api.getWorkspaceChats(workspaceId, pageSize, pageToken)
    }

    @JvmOverloads
    fun getMyChats(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): CompletableFuture<GetChatsResponse> = async {
        api.getMyChats(workspaceId, pageSize, pageToken)
    }

    fun getInfo(workspaceId: String, chatIds: List<String>): CompletableFuture<List<ChatMembership>> = async {
        api.getInfo(workspaceId, chatIds)
    }

    fun leave(workspaceId: String, chatId: String): CompletableFuture<Void> = asyncVoid {
        api.leave(workspaceId, chatId)
    }

    fun archive(workspaceId: String, chatId: String): CompletableFuture<Void> = asyncVoid {
        api.archive(workspaceId, chatId)
    }

    fun unarchive(workspaceId: String, chatId: String): CompletableFuture<Void> = asyncVoid {
        api.unarchive(workspaceId, chatId)
    }

    fun setMemberRole(workspaceId: String, chatId: String, membershipId: String, role: ChatRole): CompletableFuture<Void> = asyncVoid {
        api.setMemberRole(workspaceId, chatId, membershipId, role)
    }

    fun inviteV2(workspaceId: String, chatId: String, membershipIds: List<String>): CompletableFuture<Void> = asyncVoid {
        api.inviteV2(workspaceId, chatId, membershipIds)
    }

    fun kickV2(workspaceId: String, chatId: String, membershipIds: List<String>): CompletableFuture<Void> = asyncVoid {
        api.kickV2(workspaceId, chatId, membershipIds)
    }

    @JvmOverloads
    fun createUserEventsChat(workspaceId: String, eventsType: EventsType? = null): CompletableFuture<CreateChatResponse> = async {
        api.createUserEventsChat(workspaceId, eventsType)
    }

    @JvmOverloads
    fun createWorkspaceChatV2(
        workspaceId: String,
        name: String,
        workspaceChatType: WorkspaceChatType,
        participants: List<String>? = null,
        announce: Boolean? = null,
        autoJoinNewMembers: Boolean? = null,
        description: String? = null
    ): CompletableFuture<CreateChatResponse> = async {
        api.createWorkspaceChatV2(workspaceId, name, workspaceChatType, participants, announce, autoJoinNewMembers, description)
    }

    fun getOrCreatePersonalChat(workspaceId: String, participant: String): CompletableFuture<CreateChatResponse> = async {
        api.getOrCreatePersonalChat(workspaceId, participant)
    }

    fun getOrCreateThreadChat(
        workspaceId: String,
        parentChatId: String,
        parentMessageId: String
    ): CompletableFuture<CreateChatResponse> = async {
        api.getOrCreateThreadChat(workspaceId, parentChatId, parentMessageId)
    }
}
