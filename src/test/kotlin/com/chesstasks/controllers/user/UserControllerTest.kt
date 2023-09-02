package com.chesstasks.controllers.user

import com.chesstasks.data.dto.*
import com.chesstasks.services.user.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.h2.util.json.JSONArray
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserControllerTest : BaseWebTest() {

    @Test
    fun `searchByUsernameEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("/api/user/search/by-username?query=kacper").status.isForbid()
    }

    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[emailAddress] = "kacperf1234@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }
    }

    private fun setupAdmin() = transaction {
        Admins.insert {
            it[userId] = 0
            it[id] = 0
        }
    }

    private fun setupUsers() = transaction {
        Users.insert {
            it[id] = 1
            it[username] = "glizda88"
            it[emailAddress] = "glizda88@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        Users.insert {
            it[id] = 2
            it[username] = "glizdka"
            it[emailAddress] = "glizdka@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }
    }

    @Test
    fun `searchByUsernameEndpoint returns OK if authenticated`() = testSuspend {
        setupUser()
        app.client.get("/api/user/search/by-username?query=kacper"){withToken(0)}.status.isOk()
    }

    @Test
    fun `searchByUsernameEndpoint returns expected items length and OK - scenario 1`() = testSuspend {
        setupUser()
        setupUsers()
        val resp = app.client.get("/api/user/search/by-username?query=kacper"){withToken(0)}
        resp.jsonPath("$.length()", 1)
    }

    @Test
    fun `searchByUsernameEndpoint returns expected items length and OK - scenario 2`() = testSuspend {
        setupUser()
        setupUsers()
        val resp = app.client.get("/api/user/search/by-username?query=glizd"){withToken(0)}
        resp.jsonPath("$.length()", 2)
    }

    // TODO: Test returned data [list items] are good.

    private fun setupTestData() = transaction {
        Users.insert {
            it[passwordHash] = "Test"
            it[emailAddress] = "test@gmail.com"
            it[username] = "test"
            it[id] = 1
        }
    }

    @Test
    fun `deleteUserAsAdminEndpoint returns FORBIDDEN if no admin`() = testSuspend {
        setupTestData()
        setupUser()
        app.client.delete("/api/user/as-admin/1"){withToken(0)}.status.isForbid()
    }

    @Test
    fun `deleteUserAsAdminEndpoint returns BAD_REQUEST if admin but target user does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.delete("/api/user/as-admin/1"){withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `deleteUserAsAdminEndpoint returns NO_CONTENT if admin and target user does exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupTestData()
        app.client.delete("/api/user/as-admin/1"){withToken(0)}.status.isNoContent()
    }

    private fun getUser(id: Int): UserDto? = transaction {
        Users
            .select { Users.id eq id }
            .map(UserDto::tryFrom)
            .singleOrNull()
    }

    @Test
    fun `deleteUserAsAdminEndpoint returns NO_CONTENT and deletes user from database`() = testSuspend {
        setupUser()
        setupAdmin()
        setupTestData()

        assertNotNull(getUser(1))

        app.client.delete("/api/user/as-admin/1"){withToken(0)}.status.isNoContent()

        assertNull(getUser(1))
    }

    private fun getFriendship(id: Int): FriendDto? = transaction {
        Friends.select {Friends.id eq id}
            .map(FriendDto::from)
            .singleOrNull()
    }

    @Test
    fun `deleteUserAsAdminEndpoint returns NO_CONTENT and deletes friendships user had`() = testSuspend {
        setupUser()
        setupAdmin()
        setupTestData()

        transaction {
            Friends.insert {
                it[userId] = 0
                it[secondUserId] = 1
                it[id] = 0
            }
        }

        app.client.delete("/api/user/as-admin/1"){withToken(0)}.status.isNoContent()

        assertNull(getUser(1))
        assertNull(getFriendship(0))
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns FORBID if authenticated is not an admin`() = testSuspend {
        setupUser()
        app.client.post("/api/user/as-admin/all/filtered"){withToken(0)}.status.isForbid()
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns 415 if authenticated as admin but no JSON body given`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/user/as-admin/all/filtered"){withToken(0)}.status.isUnsupportedMediaType()
    }

    private fun HttpRequestBuilder.filteredList(usernameLike: String = "", emailAddressLike: String = "") {
        jsonBody(
            "usernameLike" to usernameLike,
            "emailLike" to emailAddressLike
        )
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK if authenticated as admin and valid body given`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/user/as-admin/all/filtered"){withToken(0); filteredList()}.status.isOk()
    }

    private fun setupRandomUsersWithUsernameLike(ids: IntRange = 0..100, usernameContain: String = "a", emailContain: String = "") = transaction {
        ids.forEach { i ->
            Users.insert {
                it[username] = "${usernameContain}${i}"
                it[emailAddress] = "${emailContain}${i}@gmail.com"
                it[id] = i
                it[passwordHash] = "Test"
            }
        }
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and single result if no more users and filters are empty`() = testSuspend {
        setupUser()
        setupAdmin()
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList() }
        r.status.isOk()
        r.jsonPath("$.length()", 1)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and expected results using filters for username`() = testSuspend {
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..45, "Aneta")
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList("Aneta") }
        r.status.isOk()
        r.jsonPath("$.length()", 45)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and expected items using filters for username`() = testSuspend {
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..200, "Aneta")
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList("Aneta") }
        r.jsonPath("$[0].id", 1)
        r.jsonPath("$[1].id", 2)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and expected results using filters for username and skip`() = testSuspend {
        val skip = 50
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..200, "Aneta")
        val r = app.client.post("/api/user/as-admin/all/filtered?skip=$skip") { withToken(0); filteredList("Aneta") }
        r.jsonPath("$[0].id", 1 + skip)
        r.jsonPath("$[1].id", 2 + skip)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and expected items length using filters for username and limit`() = testSuspend {
        val skip = 50
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..200, "Aneta")
        val r = app.client.post("/api/user/as-admin/all/filtered?skip=$skip&limit=10") { withToken(0); filteredList("Aneta") }
        r.jsonPath("$.length()", 10)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and default limit equals to UserService,DEFAULT_USERS_LIST_LIMIT`() = testSuspend {
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..200, "Aneta")
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList("Aneta") }
        r.jsonPath("$.length()", UserService.DEFAULT_USERS_LIST_LIMIT)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and expected results using filters for emailAddress`() = testSuspend {
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..15, usernameContain = "username", emailContain = "Anetka")
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList(emailAddressLike = "Anetka") }
        r.jsonPath("$.length()", 15)
    }

    @Test
    fun `getFilteredListAsAdminEndpoint returns OK and empty if filters matches nothing`() = testSuspend {
        setupUser()
        setupAdmin()
        setupRandomUsersWithUsernameLike(1..15, usernameContain = "username", emailContain = "Anetka")
        val r = app.client.post("/api/user/as-admin/all/filtered") { withToken(0); filteredList(emailAddressLike = "JebacDisa") }
        r.jsonPath("$.length()", 0)
    }

    @Test
    fun `getUserAsAdminEndpoint returns FORBID if authenticated as user, not as admin`() = testSuspend {
        setupUser()
        app.client.get("/api/user/as-admin/by-id/0"){withToken(0)}.status.isForbid()
    }

    @Test
    fun `getUserAsAdminEndpoint returns OK if authenticated as admin, and user does exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/user/as-admin/by-id/0"){withToken(0)}.status.isOk()
    }

    @Test
    fun `getUserAsAdminEndpoint returns BAD_REQUEST if authenticated as admin, but user does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/user/as-admin/by-id/1"){withToken(0)}.status.isBadRequest()
    }
}