package ru.rt.yuchatbotapi.api

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MembersApiMockTest {

    @Test
    fun `list v1 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            jsonResponse("""{"members":[
                {"memberId":"m1","roleType":"MEMBER","status":"ACTIVE"},
                {"memberId":"m2","roleType":"ADMIN","status":"ACTIVE"}
            ]}""")
        }
        val api = MembersApi(client)

        val result = api.list("ws-1")

        assertEquals("/public/v1/member.list", capturedPath)
        assertEquals(2, result.size)
        assertEquals("m1", result[0].memberId)
    }

    @Test
    fun `list v1 handles null members`() = runBlocking {
        val client = createMockClient { _ -> jsonResponse("""{"members":null}""") }
        val api = MembersApi(client)

        val result = api.list("ws-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMembers v2 calls correct endpoint with pagination`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"membershipIds":["m1","m2"],"nextPageToken":"next-1"}""")
        }
        val api = MembersApi(client)

        val result = api.getMembers("ws-1", pageSize = 10, pageToken = "page-0")

        assertEquals("/public/v2/getMembers", capturedPath)
        assertEquals(2, result.membershipIds.size)
        assertEquals("next-1", result.nextPageToken)
        assertTrue(capturedBody.contains("\"pageSize\":10"))
        assertTrue(capturedBody.contains("\"pageToken\":\"page-0\""))
    }

    @Test
    fun `invite v2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = MembersApi(client)

        api.invite("ws-1", listOf("user@corp.ru"))

        assertEquals("/public/v2/inviteMember", capturedPath)
        assertTrue(capturedBody.contains("\"emails\":[\"user@corp.ru\"]"))
    }

    @Test
    fun `remove v2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = MembersApi(client)

        api.remove("ws-1", "member-1")

        assertEquals("/public/v2/removeMember", capturedPath)
        assertTrue(capturedBody.contains("\"membershipId\":\"member-1\""))
    }

    @Test
    fun `setRole v2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            emptyOkResponse()
        }
        val api = MembersApi(client)

        api.setRole("ws-1", "member-1", ru.rt.yuchatbotapi.model.WorkspaceRole.ADMIN)

        assertEquals("/public/v2/setWorkspaceMemberRole", capturedPath)
        assertTrue(capturedBody.contains("\"membershipId\":\"member-1\""))
        assertTrue(capturedBody.contains("\"role\":\"ADMIN\""))
    }

    @Test
    fun `getInfo v2 calls correct endpoint`() = runBlocking {
        var capturedPath = ""
        var capturedBody = ""
        val client = createMockClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.bodyAsText()
            jsonResponse("""{"members":[{
                "membershipId":"m1",
                "profile":{"accountId":"a1","createdAt":"2024-01-01","updatedAt":"2024-01-01","accountType":"BOT"},
                "createdAt":"2024-01-01","updatedAt":"2024-01-01",
                "presence":{"online":true,"onCall":false,"since":"2024-01-01"},
                "memberStatus":"ACTIVE"
            }]}""")
        }
        val api = MembersApi(client)

        val result = api.getInfo("ws-1", listOf("m1"))

        assertEquals("/public/v2/getMembersInfo", capturedPath)
        assertEquals(1, result.size)
        assertEquals("m1", result[0].membershipId)
        assertTrue(capturedBody.contains("\"workspaceId\":\"ws-1\""))
    }
}
