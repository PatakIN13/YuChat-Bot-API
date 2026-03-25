package ru.rt.yuchatbotapi.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import ru.rt.yuchatbotapi.internal.YuChatHttpClient
import ru.rt.yuchatbotapi.model.*
import java.io.File

/**
 * Работа с файлами.
 *
 * [getUploadUrl] и [getDownloadUrl] используют API v1.
 * [upload] — высокоуровневый helper (получает pre-signed URL + загружает файл по PUT).
 * [getUploadUrlV2] и [getDownloadUrlV2] используют API v2.
 */
class FilesApi internal constructor(private val client: YuChatHttpClient) {

    /**
     * Получение pre-signed URL для загрузки файла (v1).
     *
     * @param workspaceId ID воркспейса
     * @param fileName имя файла
     * @param mediaType тип медиа ([MediaType])
     * @param accessChatId ID чата для ограничения доступа к файлу
     * @return [FilePreSignedResponse] с URL и fileId
     */
    suspend fun getUploadUrl(
        workspaceId: WorkspaceId,
        fileName: String,
        mediaType: MediaType,
        accessChatId: ChatId? = null
    ): FilePreSignedResponse {
        return client.post("/public/v1/file.getPreSignedUrl", GetFilePreSignedV1Request(
            workspaceId = workspaceId.value,
            fileName = fileName,
            mediaType = mediaType,
            accessChatId = accessChatId?.value
        ))
    }

    /** Получение URL для скачивания файла (v1) */
    suspend fun getDownloadUrl(fileId: String): FileDownloadResponse {
        @Serializable
        data class Req(val fileId: String)
        return client.post("/public/v1/file.getDownloadUrl", Req(fileId))
    }

    /**
     * Высокоуровневый метод: загрузка файла.
     *
     * Выполняет два шага: получает pre-signed URL, затем загружает файл по PUT.
     *
     * @param workspaceId ID воркспейса
     * @param file файл для загрузки
     * @param mediaType тип медиа (по умолчанию [MediaType.RAW])
     * @param accessChatId ID чата для ограничения доступа
     * @return fileId загруженного файла
     */
    suspend fun upload(
        workspaceId: WorkspaceId,
        file: File,
        mediaType: MediaType = MediaType.RAW,
        accessChatId: ChatId? = null
    ): String {
        val presigned = getUploadUrl(workspaceId, file.name, mediaType, accessChatId)
        client.httpClient.put(presigned.url) {
            setBody(file.readBytes())
            contentType(ContentType.Application.OctetStream)
        }
        return presigned.fileId
    }

    /** Получение URL для загрузки файла (v2) */
    suspend fun getUploadUrlV2(
        workspaceId: WorkspaceId,
        fileName: String,
        accessChatId: ChatId? = null
    ): FileUploadResponse {
        @Serializable
        data class Req(val workspaceId: String, val fileName: String, val accessChatId: String? = null)
        return client.post("/public/v2/getFileUploadUrl", Req(workspaceId.value, fileName, accessChatId?.value))
    }

    /** Получение URL для скачивания файла (v2) */
    suspend fun getDownloadUrlV2(fileId: String): FileDownloadResponse {
        @Serializable
        data class Req(val fileId: String)
        return client.post("/public/v2/getFileDownloadUrl", Req(fileId))
    }
}

@Serializable
internal data class GetFilePreSignedV1Request(
    val workspaceId: String,
    val fileName: String,
    val mediaType: MediaType,
    val accessChatId: String? = null
)
