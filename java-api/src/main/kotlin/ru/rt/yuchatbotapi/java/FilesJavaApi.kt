package ru.rt.yuchatbotapi.java

import ru.rt.yuchatbotapi.api.FilesApi
import ru.rt.yuchatbotapi.model.*
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Java-обёртка для {@link ru.rt.yuchatbotapi.api.FilesApi}.
 */
class FilesJavaApi internal constructor(private val api: FilesApi) {

    @JvmOverloads
    fun getUploadUrl(
        workspaceId: String,
        fileName: String,
        mediaType: MediaType,
        accessChatId: String? = null
    ): CompletableFuture<FilePreSignedResponse> = async {
        api.getUploadUrl(workspaceId, fileName, mediaType, accessChatId)
    }

    fun getDownloadUrl(fileId: String): CompletableFuture<FileDownloadResponse> = async {
        api.getDownloadUrl(fileId)
    }

    @JvmOverloads
    fun upload(
        workspaceId: String,
        file: File,
        mediaType: MediaType = MediaType.RAW,
        accessChatId: String? = null
    ): CompletableFuture<String> = async {
        api.upload(workspaceId, file, mediaType, accessChatId)
    }

    fun getUploadUrlV2(workspaceId: String, fileName: String, accessChatId: String? = null): CompletableFuture<FileUploadResponse> = async {
        api.getUploadUrlV2(workspaceId, fileName, accessChatId)
    }

    fun getDownloadUrlV2(fileId: String): CompletableFuture<FileDownloadResponse> = async {
        api.getDownloadUrlV2(fileId)
    }
}
