package com.chesstasks.controllers.puzzle

import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*

class PuzzleControllerTest : BaseWebTest() {
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

    private fun setupPuzzle() {
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
        }
    }

    @Test
    fun `getByIdEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        setupPuzzle()

        app.client.get("/puzzle/0").status.isForbid()
    }

    @Test
    fun `getByIdEndpoint returns BAD_REQUEST if authenticated but resource does not exist`() = testSuspend {
        setupUser()

        app.client.get("/puzzle/0") {withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `getByIdEndpoint returns OK if authenticated and resource does exist`() = testSuspend {
        setupUser()
        setupPuzzle()

        app.client.get("/puzzle/0") {withToken(0)}.status.isOk()
    }

    @Test
    fun `getByIdEndpoint returns expected ID and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.id", 0)
    }

    @Test
    fun `getByIdEndpoint returns expected ownerId and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.ownerId", 0)
    }

    @Test
    fun `getByIdEndpoint returns expected fen and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.fen", "8/8/8/8")
    }

    @Test
    fun `getByIdEndpoint returns expected moves and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.moves", "e2e4")
    }

    @Test
    fun `getByIdEndpoint returns expected ranking and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.ranking", 1500)
    }
}