package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.UpdatesApi
import ru.rt.yuchatbotapi.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.UpdatesApi}.
 */
class UpdatesJavaApi internal constructor(private val api: UpdatesApi) {

    @JvmOverloads
    fun getUpdates(
        offset: Long? = null,
        limit: Int? = null
    ): CompletableFuture<List<UpdateV1>> = async {
        api.getUpdates(offset, limit)
    }

    @JvmOverloads
    fun getUpdatesV2(
        offset: Long? = null,
        limit: Int? = null
    ): CompletableFuture<List<UpdateV2>> = async {
        api.getUpdatesV2(offset, limit)
    }

    @JvmOverloads
    fun setUpdateSettings(
        updateSettings: List<UpdateSetting>,
        updateApiVersion: Int,
        autoAcceptWorkspaceInvites: Boolean = false
    ): CompletableFuture<Void> = asyncVoid {
        api.setUpdateSettings(updateSettings, updateApiVersion, autoAcceptWorkspaceInvites)
    }

    fun getWorkspaceInvites(): CompletableFuture<List<WorkspaceInvite>> = async {
        api.getWorkspaceInvites()
    }

    fun acceptWorkspaceInvite(workspaceId: String): CompletableFuture<Void> = asyncVoid {
        api.acceptWorkspaceInvite(WorkspaceId(workspaceId))
    }

    fun rejectAllWorkspaceInvites(): CompletableFuture<Void> = asyncVoid {
        api.rejectAllWorkspaceInvites()
    }
}
