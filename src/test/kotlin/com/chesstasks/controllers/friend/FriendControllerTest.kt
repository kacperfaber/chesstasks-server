package com.chesstasks.controllers.friend

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.FriendRequests
import com.chesstasks.data.dto.Friends
import com.chesstasks.data.dto.Users
import com.chesstasks.randomString
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import java.util.*
import kotlin.test.*

class FriendControllerTest : BaseWebTest() {
    private fun setupUsers() {
        transaction {
            Users.insert {
                it[id] = 0
                it[username] = "kacperfaber"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }

            Users.insert {
                it[id] = 1
                it[username] = "kacperfaber's friend"
                it[emailAddress] = "friend@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }

            Users.insert {
                it[id] = 2
                it[username] = "Noone"
                it[emailAddress] = "no-one@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    private fun setupFriend() {
        transaction {
            Friends.insert {
                it[id] = 0
                it[userId] = 0
                it[secondUserId] = 1
            }

            Friends.insert {
                it[id] = 1
                it[userId] = 2
                it[secondUserId] = 0
            }
        }
    }

    @Test
    fun `deleteFriendByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.delete("/api/friend/by-id/0").status.isForbid()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST authenticated but resource does not exist`() = testSuspend {
        setupUsers()
        app.client.delete("/api/friend/by-id/0") { withToken(0) }.status.isBadRequest()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated and resource exist`() = testSuspend {
        setupUsers()
        setupFriend()
        app.client.delete("/api/friend/by-id/0") { withToken(0) }.status.isNoContent()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated as (Friends-userId) and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/api/friend/by-id/0") { withToken(0) }.status.isNoContent()
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated as (Friends-secondUserId) and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/api/friend/by-id/0") { withToken(1) }.status.isNoContent()
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST when authenticated as user with no permission and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/api/friend/by-id/0") { withToken(2) }.status.isBadRequest()
        }

    private fun hasFriendship(id: Int): Boolean = transaction {
        Friends.select { Friends.id eq id }.count() > 0
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and deletes expected resource when authenticated as (Friend-secondUserId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            assertTrue { hasFriendship(0) }

            app.client.delete("/api/friend/by-id/0") { withToken(1) }.status.isNoContent()

            assertFalse { hasFriendship(0) }
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and deletes expected resource when authenticated as (Friend-userId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            assertTrue { hasFriendship(0) }

            app.client.delete("/api/friend/by-id/0") { withToken(0) }.status.isNoContent()

            assertFalse { hasFriendship(0) }
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST and dont deletes expected resource when user has no permission to friend`() =
        testSuspend {
            setupUsers()
            setupFriend()
            assertTrue { hasFriendship(0) }

            app.client.delete("/api/friend/by-id/0") { withToken(2) }.status.isBadRequest()

            assertTrue { hasFriendship(0) }
        }

    private fun countFriends(): Long = transaction {
        Friends.selectAll().count()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and makes Friends table smaller by 1 when authenticated as (Friend-secondUserId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            val bef = countFriends()

            app.client.delete("/api/friend/by-id/0") { withToken(1) }.status.isNoContent()

            val now = countFriends()
            assertEquals(bef - 1, now)
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and and makes Friends table smaller by 1 when authenticated as (Friend-userId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            val bef = countFriends()

            app.client.delete("/api/friend/by-id/0") { withToken(0) }.status.isNoContent()

            assertEquals(bef - 1, countFriends())
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST and and makes Friends table smaller by 1 when user has no permission to friend`() =
        testSuspend {
            setupUsers()
            setupFriend()
            val bef = countFriends()

            app.client.delete("/api/friend/by-id/0") { withToken(2) }.status.isBadRequest()

            assertEquals(bef, countFriends())
        }

    private fun setupRequests() {
        transaction {
            FriendRequests.insert {
                it[FriendRequests.id] = 0
                it[senderId] = 0
                it[targetId] = 1
            }

            FriendRequests.insert {
                it[FriendRequests.id] = 1
                it[senderId] = 2
                it[targetId] = 0
            }
        }
    }

    @Test
    fun `getReceivedRequestsEndpoint returns FORBIDDEN if no auth`() = testSuspend {
        setupUsers()
        setupRequests()

        app.client.get("/api/friend/requests/received").status.isForbid()
    }

    @Test
    fun `getReceivedRequestsEndpoint returns OK when authenticated as user`() = testSuspend {
        setupUsers()
        setupRequests()

        app.client.get("/api/friend/requests/received") { withToken(0) }.status.isOk()
    }

    private fun setupAdmin() {
        transaction {
            Admins.insert {
                it[id] = 0
                it[userId] = 0
            }
        }
    }

    @Test
    fun `getReceivedRequestsEndpoint returns OK when authenticated as admin`() = testSuspend {
        setupUsers()
        setupAdmin()
        setupRequests()

        app.client.get("/api/friend/requests/received") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getReceivedRequestsEndpoint returns OK expected items count`() = testSuspend {
        setupUsers()
        setupRequests()

        val response = app.client.get("/api/friend/requests/received") { withToken(1) }
        response.status.isOk()
        response.jsonPath("$.length()", 1)
    }

    @Test
    fun `getReceivedRequestsEndpoint returns expected data`() = testSuspend {
        setupUsers()
        setupRequests()

        val response = app.client.get("/api/friend/requests/received") { withToken(0) }
        response.status.isOk()
        response.jsonPath("$[0].id", 1)
        response.jsonPath("$[0].senderId", 2)
    }

    @Test
    fun `getAllFriendEndpoint returns FORBIDDEN if no auth`() = testSuspend {
        setupUsers()
        setupFriend()
        app.client.get("/api/friend/all").status.isForbid()
    }

    @Test
    fun `getAllFriendEndpoint returns OK when authenticated as user`() = testSuspend {
        setupUsers()
        setupFriend()
        app.client.get("/api/friend/all") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllFriendEndpoint returns OK when authenticated as admin`() = testSuspend {
        setupUsers()
        setupAdmin()
        setupFriend()
        app.client.get("/api/friend/all") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllFriendEndpoint returns OK and expected items length`() = testSuspend {
        setupUsers()
        setupAdmin()
        setupFriend()
        val resp = app.client.get("/api/friend/all") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 2)
    }

    private fun setupRandomFriends(r: Int, add: Int = 5, secUserId: Int = 0) {
        transaction {
            repeat(r) { i ->
                Users.insert {
                    it[id] = i + add
                    it[username] = randomString(12)
                    it[emailAddress] = randomString(12)
                    it[passwordHash] = "HelloWorld123"
                }

                Friends.insert {
                    it[id] = i + add
                    it[userId] = i + add
                    it[secondUserId] = secUserId
                }
            }
        }
    }

    @Test
    fun `getAllFriendEndpoint returns OK and 50 items when there's too much`() = testSuspend {
        setupUsers()
        setupRandomFriends(777, 10)
        val resp = app.client.get("/api/friend/all") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 50)
    }

    @Test
    fun `getAllFriendEndpoint returns OK and expected data`() = testSuspend {
        setupUsers()
        setupRandomFriends(1, 10)
        val resp = app.client.get("/api/friend/all") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].id", 10)
        resp.jsonPath("$[0].secondUserId", 0)
        resp.jsonPath("$[0].userId", 10)
    }

    @Test
    fun `getAllFriendEndpoint returns OK and expected items USING SKIP`() = testSuspend {
        setupUsers()
        setupRandomFriends(1000, 10)
        val resp = app.client.get("/api/friend/all?skip=500") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].id", 10 + 500)
        resp.jsonPath("$[0].secondUserId", 0)
        resp.jsonPath("$[0].userId", 10 + 500)
    }

    @Test
    fun `getAllFriendEndpoint returns OK and expected items when skip not set`() = testSuspend {
        setupUsers()
        setupRandomFriends(1000, 10)
        val resp = app.client.get("/api/friend/all") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].id", 10)
        resp.jsonPath("$[0].secondUserId", 0)
        resp.jsonPath("$[0].userId", 10)
    }

    @Test
    fun `putFriendRequestEndpoint returns FORBIDDEN when no auth`() = testSuspend {
        setupUsers()
        app.client.put("/api/friend/requests").status.isForbid()
    }

    @Test
    fun `putFriendRequestEndpoint returns OK when valid data passed and authenticated`() = testSuspend {
        setupUsers()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isOk()
    }

    @Test
    fun `putFriendRequestEndpoint returns BAD_REQUEST authenticated but target user does not exist`() = testSuspend {
        setupUsers()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 199)
        }.status.isBadRequest()
    }

    private fun getRequestByUsers(userId: Int, secondUserId: Int): ResultRow? {
        return transaction {
            FriendRequests.select { (FriendRequests.targetId eq secondUserId) and (FriendRequests.senderId eq userId) }
                .singleOrNull()
        }
    }

    @Test
    fun `putFriendRequestEndpoint makes FriendRequest row when returns OK`() = testSuspend {
        setupUsers()

        assertNull(getRequestByUsers(0, 1))

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isOk()

        assertNotNull(getRequestByUsers(0, 1))
    }

    private fun countRequests(): Long = transaction {
        FriendRequests.selectAll().count()
    }

    @Test
    fun `putFriendRequestEndpoint doesnt make FriendRequest row when returns BAD_REQUEST`() = testSuspend {
        setupUsers()

        assertNull(getRequestByUsers(0, 199))

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 199)
        }.status.isBadRequest()

        assertNull(getRequestByUsers(0, 199))
    }

    @Test
    fun `putFriendRequestEndpoint makes FriendRequests table bigger by 1 when returns OK`() = testSuspend {
        setupUsers()

        val bef = countRequests()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isOk()

        assertEquals(bef + 1, countRequests())
    }

    @Test
    fun `putFriendRequestEndpoint doesnt make FriendRequests table bigger by 1 when returns OK`() = testSuspend {
        setupUsers()

        val bef = countRequests()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 199)
        }.status.isBadRequest()

        assertEquals(bef, countRequests())
    }

    @Test
    fun `putFriendRequestEndpoint returns BAD_REQUEST when they're already friends`() = testSuspend {
        setupUsers()
        setupFriend()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isBadRequest()
    }

    @Test
    fun `putFriendRequestEndpoint doesnt make row in FriendRequests when they're already friends`() = testSuspend {
        setupUsers()
        setupFriend()

        val bef = countRequests()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isBadRequest()

        assertEquals(bef, countRequests())
    }

    private fun setupFriendRequests() {
        transaction {
            FriendRequests.insert {
                it[id] = 0
                it[senderId] = 0
                it[targetId] = 1
            }
        }
    }

    @Test
    fun `putFriendRequestEndpoint returns BAD_REQUEST when FriendRequest already created`() = testSuspend {
        setupUsers()
        setupFriendRequests()

        val bef = countRequests()

        app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }.status.isBadRequest()

        assertEquals(bef, countRequests())
    }

    @Test
    fun `putFriendRequestEndpoint returns BAD_REQUEST when FriendRequest already created - scenario TARGET_ID sent`() =
        testSuspend {
            setupUsers()

            transaction {
                FriendRequests.insert {
                    it[id] = 0
                    it[senderId] = 1
                    it[targetId] = 0
                }
            }

            val bef = countRequests()

            app.client.put("/api/friend/requests") {
                withToken(0)
                jsonBody("userId" to 1)
            }.status.isBadRequest()

            assertEquals(bef, countRequests())
        }

    @Test
    fun `putFriendRequestEndpoint returns UNSUPPORTED_MEDIA_TYPE when no body`() = testSuspend {
        setupUsers()

        assertEquals(HttpStatusCode.UnsupportedMediaType, app.client.put("/api/friend/requests") { withToken(0) }.status)
    }

    @Test
    fun `putFriendRequestEndpoint returns OK and expected data`() = testSuspend {
        setupUsers()

        val r = app.client.put("/api/friend/requests") {
            withToken(0)
            jsonBody("userId" to 1)
        }
        r.status.isOk()
        val expectedIdFromDb = getRequestByUsers(0, 1)?.get(FriendRequests.id)
        r.jsonPath("$.id", expectedIdFromDb)
        r.jsonPath("$.senderId", 0)
        r.jsonPath("$.targetId", 1)
    }

    @Test
    fun `getSentRequestsEndpoint returns FORBIDDEN if no auth`() = testSuspend {
        setupUsers()
        app.client.get("/api/friend/requests/sent").status.isForbid()
    }

    @Test
    fun `getSentRequestsEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUsers()
        setupAdmin()
        app.client.get("/api/friend/requests/sent"){withToken(0)}.status.isOk()
    }

    @Test
    fun `getSentRequestsEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUsers()
        app.client.get("/api/friend/requests/sent"){withToken(0)}.status.isOk()
    }

    private fun setupRandomRequests(iter: Int, offset: Int, senderUserId: Int) = transaction {
        repeat(iter) {iteration ->
            val id = iteration + offset

            Users.insert {
                it[Users.id] = id
                it[emailAddress] = UUID.randomUUID().toString().take(24)
                it[username] = UUID.randomUUID().toString().take(24)
                it[passwordHash] = "HelloWorld123"
            }

            FriendRequests.insert {
                it[FriendRequests.id] = id
                it[senderId] = senderUserId
                it[targetId] = id
            }
        }
    }

    @Test
    fun `getSentRequestsEndpoint returns expected items length max=50 and OK`() = testSuspend {
        setupUsers()
        setupRandomRequests(100, 10, 0)

        val r = app.client.get("/api/friend/requests/sent") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 50)
    }

    @Test
    fun `getSentRequestsEndpoint returns expected items length and OK`() = testSuspend {
        setupUsers()
        setupRandomRequests(25, 10, 0)

        val r = app.client.get("/api/friend/requests/sent") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 25)
    }

    @Test
    fun `getSentRequestsEndpoint returns OK and expected first item`() = testSuspend {
        setupUsers()
        setupRandomRequests(500, 10, 0)

        val r = app.client.get("/api/friend/requests/sent") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 10)
    }

    @Test
    fun `getSentRequestsEndpoint returns OK and expected first item using skip`() = testSuspend {
        setupUsers()
        setupRandomRequests(500, 10, 0)

        val r = app.client.get("/api/friend/requests/sent?skip=50") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 60)
    }
}