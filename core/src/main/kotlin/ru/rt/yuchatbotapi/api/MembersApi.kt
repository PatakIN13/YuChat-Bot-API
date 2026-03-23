package ru.rt.yuchatbotapi.api

import kotlinx.serialization.Serializable
import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*

/**
 * Работа с участниками воркспейса.
 *
 * [list] → API v1. [getMembers], [invite], [remove], [setRole], [getInfo] → API v2.
 */
class MembersApi internal constructor(private val client: YuChatHttpClient) {

    // ── v1 ──

    /** Список участников воркспейса (v1) */
    suspend fun list(workspaceId: String): List<Member> {
        @Serializable
        data class Req(val workspaceId: String)
        @Serializable
        data class Resp(val members: List<Member>? = null)
        return client.post<Resp>("/public/v1/member.list", Req(workspaceId)).members ?: emptyList()
    }

    // ── v2 only ──

    /** Получение списка участников воркспейса (v2, пагинация) */
    suspend fun getMembers(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetMembersResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getMembers", Req(workspaceId, pageSize, pageToken))
    }

    /** Приглашение участников в воркспейс (v2) */
    suspend fun invite(workspaceId: String, emails: List<String>) {
        @Serializable
        data class Req(val workspaceId: String, val emails: List<String>)
        client.postNoContent("/public/v2/inviteMember", Req(workspaceId, emails))
    }

    /** Удаление участника из воркспейса (v2) */
    suspend fun remove(workspaceId: String, membershipId: String) {
        @Serializable
        data class Req(val workspaceId: String, val membershipId: String)
        client.postNoContent("/public/v2/removeMember", Req(workspaceId, membershipId))
    }

    /** Изменение роли участника в воркспейсе (v2) */
    suspend fun setRole(workspaceId: String, membershipId: String, role: WorkspaceRole) {
        @Serializable
        data class Req(val workspaceId: String, val membershipId: String, val role: WorkspaceRole)
        client.postNoContent("/public/v2/setWorkspaceMemberRole", Req(workspaceId, membershipId, role))
    }

    /** Информация об участниках (v2) */
    suspend fun getInfo(workspaceId: String, membershipIds: List<String>): List<MemberInfo> {
        @Serializable
        data class Req(val workspaceId: String, val membershipIds: List<String>)
        @Serializable
        data class Resp(val members: List<MemberInfo>)
        return client.post<Resp>("/public/v2/getMembersInfo", Req(workspaceId, membershipIds)).members
    }
}
