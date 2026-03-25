package ru.rt.yuchatbotapi.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemberSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        isLenient = true
    }

    @Test
    fun `deserialize v1 Member`() {
        val raw = """
        {
            "memberId": "m1",
            "profile": {
                "profileId": "p1",
                "primaryEmail": "user@corp.ru",
                "fullName": "Иванов Иван",
                "type": "REGULAR",
                "details": {
                    "position": "Разработчик",
                    "department": "IT",
                    "location": "OFFICE"
                }
            },
            "roleType": "MEMBER",
            "presence": {
                "isOnline": true,
                "isOnCall": false
            },
            "status": "ACTIVE"
        }
        """.trimIndent()

        val member = json.decodeFromString<Member>(raw)
        assertEquals(MembershipId("m1"), member.memberId)
        assertEquals("Иванов Иван", member.profile?.fullName)
        assertEquals(AccountType.REGULAR, member.profile?.type)
        assertEquals("Разработчик", member.profile?.details?.position)
        assertEquals(AccountLocation.OFFICE, member.profile?.details?.location)
        assertEquals(true, member.presence?.isOnline)
        assertEquals(MemberStatus.ACTIVE, member.status)
    }

    @Test
    fun `deserialize v2 MemberInfo`() {
        val raw = """
        {
            "membershipId": "gIQffAsGi8",
            "profile": {
                "accountId": "AIQffAsGi8",
                "email": "i.ivanov@example.ru",
                "createdAt": "2023-02-26T18:58:36.154+03:00",
                "updatedAt": "2024-02-26T18:58:36.154+03:00",
                "fullName": "Иванов Иван",
                "accountType": "REGULAR",
                "accountDetails": {
                    "position": "Аналитик",
                    "department": "B2B",
                    "bio": "Не беспокоить после 20:00"
                }
            },
            "createdAt": "2023-02-26T18:58:36.154+03:00",
            "updatedAt": "2024-02-26T18:58:36.154+03:00",
            "workspaceRole": "ADMIN",
            "presence": {
                "online": true,
                "onCall": false,
                "since": "2024-02-26T18:58:36.154+03:00"
            },
            "timeZone": "Europe/Moscow",
            "memberStatus": "ACTIVE"
        }
        """.trimIndent()

        val info = json.decodeFromString<MemberInfo>(raw)
        assertEquals(MembershipId("gIQffAsGi8"), info.membershipId)
        assertEquals("Иванов Иван", info.profile.fullName)
        assertEquals(WorkspaceRole.ADMIN, info.workspaceRole)
        assertEquals(true, info.presence.online)
        assertEquals("Europe/Moscow", info.timeZone)
        assertEquals("Не беспокоить после 20:00", info.profile.accountDetails?.bio)
    }

    @Test
    fun `deserialize v2 MemberInfo bot account`() {
        val raw = """
        {
            "membershipId": "mb1",
            "profile": {
                "accountId": "a1",
                "createdAt": "2023-01-01T00:00:00Z",
                "updatedAt": "2023-01-01T00:00:00Z",
                "accountType": "BOT"
            },
            "createdAt": "2023-01-01T00:00:00Z",
            "updatedAt": "2023-01-01T00:00:00Z",
            "presence": {"online": false, "onCall": false, "since": "2023-01-01T00:00:00Z"},
            "memberStatus": "ACTIVE"
        }
        """.trimIndent()

        val info = json.decodeFromString<MemberInfo>(raw)
        assertEquals(AccountType.BOT, info.profile.accountType)
        assertNull(info.profile.fullName)
        assertNull(info.workspaceRole)
    }
}
