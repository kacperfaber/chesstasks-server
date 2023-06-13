package com.chesstasks.controllers.puzzle.history

import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleHistoryItems
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertNull

class PuzzleHistoryControllerTest : BaseWebTest() {
    private fun setupUser() {
        transaction {
            Users.insert {
                it[id] = 0
                it[username] = "kacperfaber"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    private fun setupUser2() {
        transaction {
            Users.insert {
                it[id] = 1
                it[username] = "kacperfaber2"
                it[emailAddress] = "kacperf2@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    private fun setupPuzzleHistory() {
        transaction {
            Puzzles.insert {
                it[id] = 0
                it[ownerId] = 0
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
                it[themeIds] = "AAAA,BBBB"
            }

            PuzzleHistoryItems.insert {
                it[id] = 0
                it[userId] = 0
                it[puzzleId] = 0
                it[moves] = "e2e4"
                it[createdAt] = System.currentTimeMillis()
            }
        }
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication passed`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns BAD_REQUEST if authenticated as user but resource does not exist`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/history/0"){withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns BAD_REQUEST if authenticated as user but user_id not match`() = testSuspend {
        setupUser()
        setupUser2()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0"){withToken(1)}.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns OK if authenticated as user and resource exists`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0"){withToken(0)}.status.isOk()
    }

    @Test
    fun `byIdEndpoint returns OK and expected ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.id", 0)
    }

    @Test
    fun `byIdEndpoint returns OK and expected USER_ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.userId", 0)
    }

    @Test
    fun `byIdEndpoint returns OK and expected PUZZLE_ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.puzzleId", 0)
    }

    @Test
    fun `byIdEndpoint returns NULL or EMPTY 'user' object when OK`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        assertNull(r.jsonPath("$.user"))
    }

    @Test
    fun `byIdEndpoint returns NULL or EMPTY 'puzzle' object when OK`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        assertNull(r.jsonPath("$.puzzle"))
    }
}