package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.MembersApi
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.MembersApi}.
 */
class MembersJavaApi internal constructor(private val api: MembersApi) {

    fun list(workspaceId: String): CompletableFuture<List<Member>> = async {
        api.list(workspaceId)
    }

    @JvmOverloads
    fun getMembers(
        workspaceId: String,
        pageSize: Int? = null,
        pageToken: String? = null
    ): CompletableFuture<GetMembersResponse> = async {
        api.getMembers(workspaceId, pageSize, pageToken)
    }

    fun invite(workspaceId: String, emails: List<String>): CompletableFuture<Void> = asyncVoid {
        api.invite(workspaceId, emails)
    }

    fun remove(workspaceId: String, membershipId: String): CompletableFuture<Void> = asyncVoid {
        api.remove(workspaceId, membershipId)
    }

    fun setRole(workspaceId: String, membershipId: String, role: WorkspaceRole): CompletableFuture<Void> = asyncVoid {
        api.setRole(workspaceId, membershipId, role)
    }

    fun getInfo(workspaceId: String, membershipIds: List<String>): CompletableFuture<List<MemberInfo>> = async {
        api.getInfo(workspaceId, membershipIds)
    }
}
