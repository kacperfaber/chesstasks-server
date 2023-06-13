package com.chesstasks.controllers.puzzle

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

        app.client.get("/puzzle/0") { withToken(0) }.status.isBadRequest()
    }

    @Test
    fun `getByIdEndpoint returns OK if authenticated and resource does exist`() = testSuspend {
        setupUser()
        setupPuzzle()

        app.client.get("/puzzle/0") { withToken(0) }.status.isOk()
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

    @Test
    fun `byLichessDbEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/all/by-database/lichess").status.isForbid()
    }

    @Test
    fun `byLichessDbEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/all/by-database/lichess") { withToken(0) }.status.isOk()
    }

    private fun setupAdmin() {
        transaction {
            Admins.insert {
                it[id] = 0
                it[userId] = 0
            }
        }
    }

    private fun setupRandomPuzzle(iteration: Int, database: PuzzleDatabase) {
        transaction {
            repeat(iteration) { iter ->
                Puzzles.insert {
                    it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                    it[moves] = "e2e4"
                    it[ownerId] = 0
                    it[id] = iter
                    it[ranking] = 1500
                    it[Puzzles.database] = database
                    it[themeIds] = "AAAA,BBBB"
                }
            }
        }

    }

    @Test
    fun `byLichessDbEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/puzzle/all/by-database/lichess") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byLichessDbEndpoint returns expected length (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        app.client.get("/puzzle/all/by-database/lichess") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byLichessDbEndpoint returns expected length with skip (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        app.client.get("/puzzle/all/by-database/lichess?skip=100") { withToken(0) }.jsonPath("$.length()", 50)
    }

    private suspend fun idRange(r: HttpResponse, ids: IntRange, skip: Int) {
        ids.forEach {
            r.jsonPath("$[${it + skip}].id", it)
        }
    }

    @Test
    fun `byLichessDbEndpoint returns items that we expect`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        val r = app.client.get("/puzzle/all/by-database/lichess") { withToken(0) }
        idRange(r, 0..49, skip=0)
    }

    @Test
    fun `byLichessDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        val r = app.client.get("/puzzle/all/by-database/lichess?skip=100") { withToken(0) }
        idRange(r, 100..149, skip=-100)
    }

    @Test
    fun `byUserDbEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/all/by-database/user").status.isForbid()
    }

    @Test
    fun `byUserDbEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/all/by-database/user") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byUserDbEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/puzzle/all/by-database/user") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byUserDbEndpoint returns expected length (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        app.client.get("/puzzle/all/by-database/user") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byUserDbEndpoint returns expected length with skip (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        app.client.get("/puzzle/all/by-database/user?skip=100") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byUserDbEndpoint returns items that we expect`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        val r = app.client.get("/puzzle/all/by-database/user") { withToken(0) }
        idRange(r, 0..49, skip=0)
    }

    @Test
    fun `byUserDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        val r = app.client.get("/puzzle/all/by-database/user?skip=100") { withToken(0) }
        idRange(r, 100..149, skip=-100)
    }
}