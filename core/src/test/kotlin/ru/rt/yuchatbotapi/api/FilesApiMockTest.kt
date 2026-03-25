package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import ru.rt.yuchatbotapi.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilesApiMockTest {

    @Test
    fun `getUploadUrl v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"url":"https://storage.example.com/upload/abc","fileId":"file-1"}""")
        }
        val api = FilesApi(client)

        val result = api.getUploadUrl(WorkspaceId("ws-1"), "report.pdf", ru.rt.yuchatbotapi.model.MediaType.PDF)

        assertEquals("/public/v1/file.getPreSignedUrl", capturedPath)
        assertEquals("file-1", result.fileId)
        assertTrue(result.url.contains("storage.example.com"))
        assertTrue(capturedBody.contains("\"mediaType\":\"PDF\""))
    }

    @Test
    fun `getDownloadUrl v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"url":"https://storage.example.com/download/file-1"}""")
        }
        val api = FilesApi(client)

        val result = api.getDownloadUrl("file-1")

        assertEquals("/public/v1/file.getDownloadUrl", capturedPath)
        assertTrue(result.url.contains("download"))
    }

    @Test
    fun `getUploadUrlV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"fileId":"file-v2","uploadUrl":"https://storage.example.com/v2/upload"}""")
        }
        val api = FilesApi(client)

        val result = api.getUploadUrlV2(WorkspaceId("ws-1"), "photo.jpg")

        assertEquals("/public/v2/getFileUploadUrl", capturedPath)
        assertEquals("file-v2", result.fileId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
        assertTrue(capturedBody.contains("\"fileName\":\"photo.jpg\""))
    }

    @Test
    fun `getDownloadUrlV2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"url":"https://storage.example.com/v2/download/file-v2"}""")
        }
        val api = FilesApi(client)

        val result = api.getDownloadUrlV2("file-v2")

        assertEquals("/public/v2/getFileDownloadUrl", capturedPath)
        assertTrue(result.url.contains("v2/download"))
    }
}
