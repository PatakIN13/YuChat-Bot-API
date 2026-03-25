package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import ru.rt.yuchatbotapi.model.*

/**
 * Итерация по всем чатам воркспейса (v2, автоматическая пагинация).
 *
 * ```kotlin
 * client.chats.getWorkspaceChatsFlow(WorkspaceId("ws-1")).collect { chatId ->
 *     println(chatId)
 * }
 * ```
 */
fun ChatsApi.getWorkspaceChatsFlow(workspaceId: WorkspaceId, pageSize: Int? = null): Flow<ChatId> = flow {
    var token: String? = null
    do {
        val page = getWorkspaceChats(workspaceId, pageSize, token)
        page.chatIds.forEach { emit(it) }
        token = page.nextPageToken
    } while (token != null)
}

/**
 * Итерация по своим чатам в воркспейсе (v2, автоматическая пагинация).
 */
fun ChatsApi.getMyChatsFlow(workspaceId: WorkspaceId, pageSize: Int? = null): Flow<ChatId> = flow {
    var token: String? = null
    do {
        val page = getMyChats(workspaceId, pageSize, token)
        page.chatIds.forEach { emit(it) }
        token = page.nextPageToken
    } while (token != null)
}

/**
 * Итерация по участникам воркспейса (v2, автоматическая пагинация).
 */
fun MembersApi.getMembersFlow(workspaceId: WorkspaceId, pageSize: Int? = null): Flow<MembershipId> = flow {
    var token: String? = null
    do {
        val page = getMembers(workspaceId, pageSize, token)
        page.membershipIds.forEach { emit(it) }
        token = page.nextPageToken
    } while (token != null)
}
