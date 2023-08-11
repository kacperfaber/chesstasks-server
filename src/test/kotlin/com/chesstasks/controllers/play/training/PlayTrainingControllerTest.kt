package com.chesstasks.controllers.play.training

import com.chesstasks.data.dto.*
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class PlayTrainingControllerTest : BaseWebTest() {
    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[emailAddress] = "kacperf1234@gmail.com"
            it[username] = "kacperfaber"
            it[passwordHash] = "HelloWorld123"
        }
    }

    private fun setupAdmin() = transaction {
        Admins.insert {
            it[id] = 0
            it[userId] = 0
        }
    }

    companion object {
        const val initialFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    private fun setupRandomPuzzles(iter: Int, database: PuzzleDatabase = PuzzleDatabase.LICHESS, ranking: Int = 1500) {
        return transaction {
            repeat(iter) {
                Puzzles.insert {
                    it[fen] = initialFEN
                    it[moves] = "e2e4 e7e5"
                    it[Puzzles.database] = database
                    it[Puzzles.ranking] = ranking
                }
            }
        }
    }

    @Test
    fun `puzzlesEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("/api/play/training/puzzles").status.isForbid()
    }

    @Test
    fun `puzzlesEndpoint returns 415 if authenticated as user and NO BODY`() = testSuspend {
        setupUser()
        app.client.get("/api/play/training/puzzles") { withToken(0) }.status.isUnsupportedMediaType()
    }

    private fun HttpRequestBuilder.getPuzzlePayload(
        rankingOffset: Int? = null,
        themeId: Int? = null,
        database: PuzzleDatabase? = null
    ) {
        jsonBody("rankingOffset" to rankingOffset, "themeId" to themeId, "database" to database)
    }

    @Test
    fun `puzzlesEndpoint returns OK if authenticated as user and BODY`() = testSuspend {
        setupUser()
        app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }.status.isOk()
    }

    @Test
    fun `puzzlesEndpoint returns OK if authenticated as admin and BODY`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }.status.isOk()
    }

    @Test
    fun `puzzlesEndpoint returns OK and expected 50 items length`() = testSuspend {
        setupUser()
        setupRandomPuzzles(500)
        val resp = app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }
        resp.status.isOk()
        resp.jsonPath("$.length()", 50)
    }

    @Test
    fun `puzzlesEndpoint returns OK and random items`() = testSuspend {
        setupUser()
        setupRandomPuzzles(10000, PuzzleDatabase.LICHESS, 1500)

        val secret = "ABC"
        createToken(0, secret)

        val r1 = app.client.get("/api/play/training/puzzles") { useToken(0, secret); getPuzzlePayload() }
        r1.status.isOk()
        val r2 = app.client.get("/api/play/training/puzzles") { useToken(0, secret); getPuzzlePayload() }
        r2.status.isOk()

        val r1FirstId = r1.jsonPath<Int>("$[0].id")
        val r2FirstId = r2.jsonPath<Int>("$[0].id")

        assertNotEquals(r1FirstId, r2FirstId)
    }

    @Test
    fun `puzzlesEndpoint returns OK and only items matching to PuzzleDatabase we want - LICHESS`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.LICHESS) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'LICHESS')].id")
        assertEquals(25, idsList?.count())
    }

    @Test
    fun `puzzlesEndpoint returns OK and only items matching to PuzzleDatabase we want - USER`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.USER) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'USER')].id")
        assertEquals(30, idsList?.count())
    }

    @Test
    fun `puzzlesEndpoint returns BAD_REQUEST if rankingOffset is greater than 1000`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.get("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.USER) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'USER')].id")
        assertEquals(30, idsList?.count())
    }

    @Test
    fun `submitPuzzleEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.post("/api/play/training/0/submit").status.isForbid()
    }

    private fun HttpRequestBuilder.submitPayload(success: Boolean? = true, moves: String? = "e2e4") {
        val pairs = listOfNotNull(
            if (success != null) "success" to success else null,
            if (moves != null) "moves" to moves else null
        ).toTypedArray()

        jsonBody(*pairs)
    }

    private fun setupRandomPuzzlesWithId(iter: Int) = transaction {
        repeat(iter) { itId ->
            Puzzles.insert {
                it[id] = itId
                it[fen] = initialFEN
                it[moves] = "e2e4"
                it[database] = PuzzleDatabase.LICHESS
                it[ranking] = 1500
            }
        }
    }

    @Test
    fun `submitPuzzleEndpoint returns 415 if authenticated as user and resource exist but no body`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(10)
        app.client.post("/api/play/training/0/submit"){withToken(0)}.status.isUnsupportedMediaType()
    }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but resource does not exist and body`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/0/submit"){withToken(0); submitPayload()}.status.isBadRequest()
    }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but missing 'success' query parameter`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/0/submit"){withToken(0); submitPayload(success = null)}.status.isBadRequest()
    }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but missing 'moves' query parameter`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/0/submit"){withToken(0); submitPayload(moves = null)}.status.isBadRequest()
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and created PuzzleHistoryItems row`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val puzzleHistoryItemBefore = transaction { PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) } }.firstOrNull()
        assertNull(puzzleHistoryItemBefore)

        app.client.post("/api/play/training/0/submit"){withToken(0); submitPayload()}.status.isOk()

        val puzzleHistoryItem = transaction { PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) } }.firstOrNull()
        assertNotNull(puzzleHistoryItem)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and created PuzzleHistoryItems row with expected data`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val moves = "d2d4 e7e5"
        val success = false

        val puzzleHistoryItemBefore = transaction { PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) } }.firstOrNull()
        assertNull(puzzleHistoryItemBefore)

        app.client.post("/api/play/training/0/submit"){withToken(0); submitPayload(success, moves)}.status.isOk()

        val puzzleHistoryItem = transaction { PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) } }.firstOrNull()
        assertEquals(moves, puzzleHistoryItem?.moves)
        assertEquals(success, puzzleHistoryItem?.success)
    }
}