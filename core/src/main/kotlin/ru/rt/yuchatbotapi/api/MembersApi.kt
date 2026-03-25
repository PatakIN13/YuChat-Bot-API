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
    suspend fun list(workspaceId: WorkspaceId): List<Member> {
        @Serializable
        data class Req(val workspaceId: String)
        @Serializable
        data class Resp(val members: List<Member>? = null)
        return client.post<Resp>("/public/v1/member.list", Req(workspaceId.value)).members ?: emptyList()
    }

    // ── v2 only ──

    /** Получение списка участников воркспейса (v2, пагинация) */
    suspend fun getMembers(
        workspaceId: WorkspaceId,
        pageSize: Int? = null,
        pageToken: String? = null
    ): GetMembersResponse {
        @Serializable
        data class Req(val workspaceId: String, val pageSize: Int? = null, val pageToken: String? = null)
        return client.post("/public/v2/getMembers", Req(workspaceId.value, pageSize, pageToken))
    }

    /** Приглашение участников в воркспейс (v2) */
    suspend fun invite(workspaceId: WorkspaceId, emails: List<String>) {
        @Serializable
        data class Req(val workspaceId: String, val emails: List<String>)
        client.postNoContent("/public/v2/inviteMember", Req(workspaceId.value, emails))
    }

    /** Удаление участника из воркспейса (v2) */
    suspend fun remove(workspaceId: WorkspaceId, membershipId: MembershipId) {
        @Serializable
        data class Req(val workspaceId: String, val membershipId: String)
        client.postNoContent("/public/v2/removeMember", Req(workspaceId.value, membershipId.value))
    }

    /** Изменение роли участника в воркспейсе (v2) */
    suspend fun setRole(workspaceId: WorkspaceId, membershipId: MembershipId, role: WorkspaceRole) {
        @Serializable
        data class Req(val workspaceId: String, val membershipId: String, val role: WorkspaceRole)
        client.postNoContent("/public/v2/setWorkspaceMemberRole", Req(workspaceId.value, membershipId.value, role))
    }

    /** Информация об участниках (v2) */
    suspend fun getInfo(workspaceId: WorkspaceId, membershipIds: List<MembershipId>): List<MemberInfo> {
        @Serializable
        data class Req(val workspaceId: String, val membershipIds: List<String>)
        @Serializable
        data class Resp(val members: List<MemberInfo>)
        return client.post<Resp>("/public/v2/getMembersInfo", Req(workspaceId.value, membershipIds.map { it.value })).members
    }
}
