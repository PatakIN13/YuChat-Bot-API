package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.MembersApi
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.MembersApi}.
 */
class MembersJavaApi internal constructor(private val api: MembersApi) {

    fun list(workspaceId: String): CompletableFuture<List<Member>> = async {
        api.list(WorkspaceId(workspaceId))
    }

    @JvmOverloads
    fun getMembers(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): CompletableFuture<GetMembersResponse> = async {
        api.getMembers(WorkspaceId(workspaceId), pageSize, pageToken)
    }

    fun invite(workspaceId: String, emails: List<String>): CompletableFuture<Void> = asyncVoid {
        api.invite(WorkspaceId(workspaceId), emails)
    }

    fun remove(workspaceId: String, membershipId: String): CompletableFuture<Void> = asyncVoid {
        api.remove(WorkspaceId(workspaceId), MembershipId(membershipId))
    }

    fun setRole(workspaceId: String, membershipId: String, role: WorkspaceRole): CompletableFuture<Void> = asyncVoid {
        api.setRole(WorkspaceId(workspaceId), MembershipId(membershipId), role)
    }

    fun getInfo(workspaceId: String, membershipIds: List<String>): CompletableFuture<List<MemberInfo>> = async {
        api.getInfo(WorkspaceId(workspaceId), membershipIds.map { MembershipId(it) })
    }
}
