package com.chesstasks.controllers.friend

import com.chesstasks.data.dto.Friends
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import testutils.isBadRequest
import testutils.isForbid
import testutils.isNoContent
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        }
    }

    @Test
    fun `deleteFriendByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.delete("/friend/by-id/0").status.isForbid()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST authenticated but resource does not exist`() = testSuspend {
        setupUsers()
        app.client.delete("/friend/by-id/0") { withToken(0) }.status.isBadRequest()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated and resource exist`() = testSuspend {
        setupUsers()
        setupFriend()
        app.client.delete("/friend/by-id/0") { withToken(0) }.status.isNoContent()
    }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated as (Friends-userId) and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/friend/by-id/0") { withToken(0) }.status.isNoContent()
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT when authenticated as (Friends-secondUserId) and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/friend/by-id/0") { withToken(1) }.status.isNoContent()
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST when authenticated as user with no permission and resource exist`() =
        testSuspend {
            setupUsers()
            setupFriend()
            app.client.delete("/friend/by-id/0") { withToken(2) }.status.isBadRequest()
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

            app.client.delete("/friend/by-id/0") { withToken(1) }.status.isNoContent()

            assertFalse { hasFriendship(0) }
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and deletes expected resource when authenticated as (Friend-userId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            assertTrue { hasFriendship(0) }

            app.client.delete("/friend/by-id/0") { withToken(0) }.status.isNoContent()

            assertFalse { hasFriendship(0) }
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST and dont deletes expected resource when user has no permission to friend`() =
        testSuspend {
            setupUsers()
            setupFriend()
            assertTrue { hasFriendship(0) }

            app.client.delete("/friend/by-id/0") { withToken(2) }.status.isBadRequest()

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

            app.client.delete("/friend/by-id/0") { withToken(1) }.status.isNoContent()

            val now = countFriends()
            assertEquals(bef - 1, now)
        }

    @Test
    fun `deleteFriendByIdEndpoint returns NO_CONTENT and and makes Friends table smaller by 1 when authenticated as (Friend-userId)`() =
        testSuspend {
            setupUsers()
            setupFriend()
            val bef = countFriends()

            app.client.delete("/friend/by-id/0") { withToken(0) }.status.isNoContent()

            assertEquals(bef - 1, countFriends())
        }

    @Test
    fun `deleteFriendByIdEndpoint returns BAD_REQUEST and and makes Friends table smaller by 1 when user has no permission to friend`() =
        testSuspend {
            setupUsers()
            setupFriend()
            val bef = countFriends()

            app.client.delete("/friend/by-id/0") { withToken(2) }.status.isBadRequest()

            assertEquals(bef, countFriends())
        }
}