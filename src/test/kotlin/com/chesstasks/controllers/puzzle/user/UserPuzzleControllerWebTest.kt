package com.chesstasks.controllers.puzzle.user

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.UserPuzzleDto
import com.chesstasks.data.dto.UserPuzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPuzzleControllerWebTest : BaseWebTest() {
    private fun setupUser() {
        transaction {
            Users.insert {
                it[Users.id] = 0
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
                it[username] = "kacperfaber"
            }
        }
    }

    private fun setupSecondUser() {
        transaction {
            Users.insert {
                it[Users.id] = 1
                it[emailAddress] = "teresajen@gmail.com"
                it[passwordHash] = "HelloWorld123"
                it[username] = "teresajen"
            }
        }
    }

    private fun setupAdmin() {
        transaction {
            Admins.insert {
                it[Admins.id] = 0
                it[userId] = 0
            }
        }
    }

    private fun setupUserPuzzle() {
        transaction {
            UserPuzzles.insert {
                it[id] = 0
                it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[ownerId] = 0
            }
        }
    }

    private fun userPuzzleLen(): Long {
        return transaction {
            UserPuzzles.selectAll().count()
        }
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication and puzzle doesnt exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/user-puzzle/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication but puzzle exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupUserPuzzle()
        app.client.get("/user-puzzle/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns BAD REQUEST if puzzle doesnt exist and authenticated`() = testSuspend {
        setupUser()
        app.client.get("/user-puzzle/0") {withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns OK and expected data when authenticated`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        val response = app.client.get("/user-puzzle/0") {withToken(0)}

        response.status.isOk()

        response.jsonPath("$.ownerId", 0)
        response.jsonPath("$.id", 0)
        response.jsonPath("$.fen", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        response.jsonPath("$.moves", "e2e4")
        response.jsonPath("$.ranking", 1500)
    }

    @Test
    fun `deleteAsOwnerEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupUserPuzzle()

        app.client.delete("/user-puzzle/as-owner/0").status.isForbid()
    }

    @Test
    fun `deleteAsOwnerEndpoint returns BAD_REQUEST if puzzle doesnt exist and authenticated`() = testSuspend {
        setupUser()
        app.client.delete("/user-puzzle/as-owner/0"){withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `deleteAsOwnerEndpoint returns BAD_REQUEST if puzzle exist but authenticated as not owner`() = testSuspend {
        setupUser()
        setupSecondUser()
        setupUserPuzzle()
        app.client.delete("/user-puzzle/as-owner/0"){withToken(1)}.status.isBadRequest()
    }

    @Test
    fun `deleteAsOwnerEndpoint returns NO_CONTENT if puzzle exist and authenticated as owner`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        app.client.delete("/user-puzzle/as-owner/0"){withToken(0)}.status.isNoContent()
    }

    @Test
    fun `deleteAsOwnerEndpoint makes UserPuzzle table shorter by 1 and returns NO_CONTENT`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        val before = userPuzzleLen()
        app.client.delete("/user-puzzle/as-owner/0"){withToken(0)}.status.isNoContent()
        val now = userPuzzleLen()
        assertEquals(before - 1, now)
    }

    private fun userPuzzleHasItem(id: Int): Boolean {
        return transaction {
            UserPuzzles.select { UserPuzzles.id eq id }.map { UserPuzzleDto::from }.any()
        }
    }

    @Test
    fun `deleteAsOwnerEndpoint deletes record from db and returns NO_CONTENT`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        assertTrue { userPuzzleHasItem(0) }
        app.client.delete("/user-puzzle/as-owner/0"){withToken(0)}.status.isNoContent()
        assertFalse { userPuzzleHasItem(0) }
    }

    @Test
    fun `deleteAsOwnerEndpoint doesnt affect records length when returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        val before = userPuzzleLen()
        app.client.delete("/user-puzzle/as-owner/0").status.isForbid()
        val now = userPuzzleLen()
        assertEquals(before, now)
    }

    @Test
    fun `deleteAsOwnerEndpoint doesnt affect records length when returns BAD_REQUEST`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        val before = userPuzzleLen()
        app.client.delete("/user-puzzle/as-owner/1"){withToken(0)}.status.isBadRequest()
        val now = userPuzzleLen()
        assertEquals(before, now)
    }
}