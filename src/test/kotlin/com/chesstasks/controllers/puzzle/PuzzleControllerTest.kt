package com.chesstasks.controllers.puzzle

import com.chesstasks.data.dto.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import java.util.UUID
import kotlin.test.*

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
            }
        }
    }

    @Test
    fun `getByIdEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        setupPuzzle()

        app.client.get("/api/puzzle/0").status.isForbid()
    }

    @Test
    fun `getByIdEndpoint returns BAD_REQUEST if authenticated but resource does not exist`() = testSuspend {
        setupUser()

        app.client.get("/api/puzzle/0") { withToken(0) }.status.isBadRequest()
    }

    @Test
    fun `getByIdEndpoint returns OK if authenticated and resource does exist`() = testSuspend {
        setupUser()
        setupPuzzle()

        app.client.get("/api/puzzle/0") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getByIdEndpoint returns expected ID and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/api/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.id", 0)
    }

    @Test
    fun `getByIdEndpoint returns expected ownerId and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/api/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.ownerId", 0)
    }

    @Test
    fun `getByIdEndpoint returns expected fen and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/api/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.fen", "8/8/8/8")
    }

    @Test
    fun `getByIdEndpoint returns expected moves and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/api/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.moves", "e2e4")
    }

    @Test
    fun `getByIdEndpoint returns expected ranking and OK`() = testSuspend {
        setupUser()
        setupPuzzle()

        val r = app.client.get("/api/puzzle/0") { withToken(0) }

        r.status.isOk()
        r.jsonPath("$.ranking", 1500)
    }

    @Test
    fun `byLichessDbEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/all/by-database/lichess").status.isForbid()
    }

    @Test
    fun `byLichessDbEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/all/by-database/lichess") { withToken(0) }.status.isOk()
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
                }
            }
        }

    }

    @Test
    fun `byLichessDbEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/puzzle/all/by-database/lichess") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byLichessDbEndpoint returns expected length (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        app.client.get("/api/puzzle/all/by-database/lichess") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byLichessDbEndpoint returns expected length with skip (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        app.client.get("/api/puzzle/all/by-database/lichess?skip=100") { withToken(0) }.jsonPath("$.length()", 50)
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
        val r = app.client.get("/api/puzzle/all/by-database/lichess") { withToken(0) }
        idRange(r, 0..49, skip = 0)
    }

    @Test
    fun `byLichessDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        val r = app.client.get("/api/puzzle/all/by-database/lichess?skip=100") { withToken(0) }
        idRange(r, 100..149, skip = -100)
    }

    @Test
    fun `byUserDbEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/all/by-database/user").status.isForbid()
    }

    @Test
    fun `byUserDbEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/all/by-database/user") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byUserDbEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/puzzle/all/by-database/user") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byUserDbEndpoint returns expected length (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        app.client.get("/api/puzzle/all/by-database/user") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byUserDbEndpoint returns expected length with skip (limit 50)`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        app.client.get("/api/puzzle/all/by-database/user?skip=100") { withToken(0) }.jsonPath("$.length()", 50)
    }

    @Test
    fun `byUserDbEndpoint returns items that we expect`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        val r = app.client.get("/api/puzzle/all/by-database/user") { withToken(0) }
        idRange(r, 0..49, skip = 0)
    }

    @Test
    fun `byUserDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        val r = app.client.get("/api/puzzle/all/by-database/user?skip=100") { withToken(0) }
        idRange(r, 100..149, skip = -100)
    }

    @Test
    fun `deleteByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/api/puzzle/0").status.isForbid()
    }

    private fun setupUser2() {
        transaction {
            Users.insert {
                it[id] = 1
                it[emailAddress] = "test@gmail.com"
                it[username] = "kacper2"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    @Test
    fun `deleteByIdEndpoint returns BAD_REQUEST if authenticated as not owner of the resource`() = testSuspend {
        setupUser()
        setupUser2()
        setupPuzzle()
        app.client.delete("/api/puzzle/0") { withToken(1) }.status.isBadRequest()
    }

    @Test
    fun `deleteByIdEndpoint returns BAD_REQUEST if authenticated but resource does not exist`() = testSuspend {
        setupUser()
        app.client.delete("/api/puzzle/0") { withToken(0) }.status.isBadRequest()
    }

    private fun countPuzzles(): Long {
        return transaction {
            Puzzles.selectAll().count()
        }
    }

    @Test
    fun `deleteByIdEndpoint returns NO_CONTENT if authenticated and resource does not exist`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/api/puzzle/0") { withToken(0) }.status.isNoContent()
    }

    @Test
    fun `deleteByIdEndpoint makes Puzzles table smaller by 1 and returns No_CONTENT`() = testSuspend {
        setupUser()
        setupPuzzle()
        val before = countPuzzles()
        app.client.delete("/api/puzzle/0") { withToken(0) }.status.isNoContent()
        val now = countPuzzles()
        assertEquals(before - 1, now)
    }

    @Test
    fun `deleteByIdEndpoint does not affect Puzzle table size when returns BAD_REQUEST`() = testSuspend {
        setupUser()
        val before = countPuzzles()
        app.client.delete("/api/puzzle/0") { withToken(0) }.status.isBadRequest()
        val now = countPuzzles()
        assertEquals(before, now)
    }

    @Test
    fun `deleteByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        val before = countPuzzles()
        app.client.delete("/api/puzzle/0").status.isForbid()
        val now = countPuzzles()
        assertEquals(before, now)
    }

    private fun getPuzzle(id: Int): PuzzleDto? {
        return transaction {
            Puzzles.select { Puzzles.id eq id }.map(PuzzleDto::from).singleOrNull()
        }
    }

    @Test
    fun `deleteByIdEndpoint deletes item we expect and returns NO_CONTENT`() = testSuspend {
        setupUser()
        setupPuzzle()
        assertNotNull(getPuzzle(0))
        app.client.delete("/api/puzzle/0") { withToken(0) }.status.isNoContent()
        assertNull(getPuzzle(0))
    }

    @Test
    fun `deleteByIdEndpoint doesnt deletes item we expect and returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        assertNotNull(getPuzzle(0))
        app.client.delete("/api/puzzle/0").status.isForbid()
        assertNotNull(getPuzzle(0))
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/api/puzzle/as-admin/0").status.isForbid()
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns FORBIDDEN if authenticated just as user`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isForbid()
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns BAD_REQUEST if authenticated as admin but resource does not exist`() =
        testSuspend {
            setupUser()
            setupAdmin()
            app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isBadRequest()
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and resource does exist`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()
            app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and deletes resource we expect`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()

            assertNotNull(getPuzzle(0))

            app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()

            assertNull(getPuzzle(0))
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and makes Puzzle table smaller by 1`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()

            val before = countPuzzles()

            app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()

            assertEquals(before - 1, countPuzzles())
        }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        setupAdmin()

        val before = countPuzzles()

        app.client.delete("/api/puzzle/as-admin/0").status.isForbid()

        assertEquals(before, countPuzzles())
    }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN - when it's just user`() =
        testSuspend {
            setupUser()
            setupPuzzle()

            val before = countPuzzles()

            app.client.delete("/api/puzzle/as-admin/0") { withToken(0) }.status.isForbid()

            assertEquals(before, countPuzzles())
        }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns BAD_REQUEST`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        setupAdmin()

        val bef = countPuzzles()
        app.client.delete("/api/puzzle/as-admin/-500") { withToken(0) }.status.isBadRequest()
        assertEquals(bef, countPuzzles())
    }

    private fun setupMateTheme() = transaction {
        Themes.insert {
            it[id] = 0
            it[name] = "mate"
        }
    }

    private fun setupMatePuzzles() {
        transaction {
            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 0
            }
        }
    }

    @Test
    fun `getAllByTheme returns FORBIDDEN if no auth`() = testSuspend {
        app.client.get("/api/puzzle/by-theme/mate").status.isForbid()
    }

    @Test
    fun `getAllByTheme returns OK auth`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupPuzzle()
        setupMatePuzzles()
        app.client.get("/api/puzzle/by-theme/mate") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllByTheme returns OK and expected items length`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupPuzzle()
        setupMatePuzzles()
        val r = app.client.get("/api/puzzle/by-theme/mate") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 1)
    }

    private fun setupRandomMatePuzzle(iteration: Int, add: Int = 0, database: PuzzleDatabase = PuzzleDatabase.LICHESS) {
        transaction {
            repeat(iteration) { iter ->
                Puzzles.insert {
                    it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                    it[moves] = "e2e4"
                    it[ownerId] = 0
                    it[id] = iter + add
                    it[ranking] = 1500
                    it[Puzzles.database] = database
                }

                PuzzleThemes.insert {
                    it[themeId] = 0
                    it[puzzleId] = iter + add
                }
            }
        }
    }

    @Test
    fun `getAllByTheme returns OK and maximum 50 items length`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupRandomMatePuzzle(500)
        val r = app.client.get("/api/puzzle/by-theme/mate") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 50)
    }

    @Test
    fun `getAllByTheme returns OK and maximum 50 items length USING SKIP`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupRandomMatePuzzle(500, 0)
        val r = app.client.get("/api/puzzle/by-theme/mate?skip=100") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 100)
        r.jsonPath("$[49].id", 149)
    }

    @Test
    fun `getAllByTheme returns OK and expected first and last item ID`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupRandomMatePuzzle(500, 0)
        val r = app.client.get("/api/puzzle/by-theme/mate") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 0)
        r.jsonPath("$[49].id", 49)
    }

    private fun setupTwoThemes() = transaction {
        Themes.insert {
            it[name] = "short"
            it[id] = 0
        }

        Themes.insert {
            it[name] = "mate"
            it[id] = 1
        }
    }

    private fun setupRandomPuzzlesWithTwoThemes(iteration: Int, add: Int) = transaction {
        repeat(iteration) { iter ->
            val id = iter + add

            Puzzles.insert {
                it[Puzzles.id] = id
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
            }

            PuzzleThemes.insert {
                it[puzzleId] = id
                it[themeId] = 0
            }

            PuzzleThemes.insert {
                it[puzzleId] = id
                it[themeId] = 1
            }
        }
    }

    @Test
    fun `getAllByThemeEndpoint returns OK and expected length if there's two themes on single puzzle`() = testSuspend {
        setupTwoThemes()
        setupRandomPuzzlesWithTwoThemes(37, 0)
        setupUser()

        app.client.get("/api/puzzle/by-theme/mate") { withToken(0) }.jsonPath("$.length()", 37)
    }

    @Test
    fun `getAllByOpeningId returns FORBIDDEN if no auth`() = testSuspend {
        app.client.get("/api/puzzle/by-opening/id/0").status.isForbid()
    }

    @Test
    fun `getAllByOpeningId returns OK if auth`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/by-opening/id/0") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllByOpeningId returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/puzzle/by-opening/id/0") { withToken(0) }.status.isOk()
    }

    private fun setupOpening() = transaction {
        Openings.insert {
            it[id] = 0
            it[eco] = "A00"
            it[moves] = "e4"
            it[name] = "Alekhine Defense"
        }
    }

    private fun setupRandomPuzzleWithOpening(iter: Int, add: Int = 0, openingId: Int = 0) = transaction {
        repeat(iter) { i ->
            val id = i + add

            Puzzles.insert {
                it[Puzzles.id] = id
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
                it[Puzzles.openingId] = openingId
            }
        }
    }

    @Test
    fun `getAllByOpeningId returns OK and expected items length`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(25)

        val r = app.client.get("/api/puzzle/by-opening/id/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 25)
    }

    @Test
    fun `getAllByOpeningId returns OK and maximum 50 items length`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(100)

        val r = app.client.get("/api/puzzle/by-opening/id/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 50)
    }

    @Test
    fun `getAllByOpeningId returns OK and 0 items length if no Puzzle with this opening`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(100)

        val r = app.client.get("/api/puzzle/by-opening/id/-1") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 0)
    }

    @Test
    fun `getAllByOpeningId returns OK and expected items`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(100)

        val r = app.client.get("/api/puzzle/by-opening/id/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 0)
        r.jsonPath("$[1].id", 1)
        r.jsonPath("$[49].id", 49)
    }

    @Test
    fun `getAllByOpeningId returns OK and expected items using SKIP`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(100)

        val skip = 50

        val r = app.client.get("/api/puzzle/by-opening/id/0?skip=$skip") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 0 + skip)
        r.jsonPath("$[1].id", 1 + skip)
        r.jsonPath("$[49].id", 49 + skip)
    }

    @Test
    fun `getAllOpeningByEco returns FORBIDDEN if no auth`() = testSuspend {
        app.client.get("/api/puzzle/by-opening/eco/A00").status.isForbid()
    }

    @Test
    fun `getAllOpeningByEco returns OK if authenticated`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllOpeningByEco returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllOpeningByEco returns OK and expected items length`() = testSuspend {
        setupUser()
        setupAdmin()
        setupOpening()
        setupRandomPuzzleWithOpening(25, 0, 0)
        val resp = app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 25)
    }

    private fun setupSecondOpening() = transaction {
        Openings.insert {
            it[id] = 1
            it[name] = "Fischers Defense"
            it[moves] = "d4"
            it[eco] = "B00"
        }
    }

    @Test
    fun `getAllOpeningByEco returns OK and 0 items length if no puzzle with this opening`() = testSuspend {
        setupUser()
        setupOpening()
        setupSecondOpening()
        setupRandomPuzzleWithOpening(50, 0, 1)
        val resp = app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 0)
    }

    @Test
    fun `getAllOpeningByEco returns OK and maximum 50 items if expected`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(250, 0, 0)
        val resp = app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 50)
    }

    @Test
    fun `getAllOpeningByEco returns OK and expected items`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(250, 0, 0)
        val resp = app.client.get("/api/puzzle/by-opening/eco/A00") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].id", 0)
        resp.jsonPath("$[49].id", 49)
    }

    @Test
    fun `getAllOpeningByEco returns OK and expected items USING SKIP`() = testSuspend {
        setupUser()
        setupOpening()
        setupRandomPuzzleWithOpening(250, 0, 0)
        val skip = 50
        val resp = app.client.get("/api/puzzle/by-opening/eco/A00?skip=$skip") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].id", 0 + skip)
        resp.jsonPath("$[49].id", 49 + skip)
    }

    private fun HttpRequestBuilder.putPuzzle(
        fen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        moves: String = "e2e4 e7e5",
        ranking: Int = 1500,
        database: PuzzleDatabase = PuzzleDatabase.LICHESS
    ) {
        jsonBody("fen" to fen, "moves" to moves, "ranking" to ranking, "database" to database)
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns FORBID if no authentication`() = testSuspend {
        app.client.put("/api/puzzle/as-admin") { putPuzzle() }.status.isForbid()
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns FORBIDDEN if authenticated as user not as admin`() = testSuspend {
        setupUser()
        app.client.put("/api/puzzle/as-admin") { putPuzzle(); withToken(0) }.status.isForbid()
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns OK if authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.put("/api/puzzle/as-admin") { putPuzzle(); withToken(0) }.status.isOk()
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns BAD_REQUEST if chess position is invalid`() = testSuspend {
        setupUser()
        setupAdmin()

        app.client.put("/api/puzzle/as-admin") {
            putPuzzle(fen = "rnbqkb1r/ppp1pppp/5n2/8/2PP4/5N2/PP3PPP/RNBQKB1R b KQkq - 2 5", moves = "e2e4");
            withToken(0)
        }.status.isBadRequest()
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns OK and makes record in database with expected data`() = testSuspend {
        setupUser()
        setupAdmin()

        val fen = "r1bq1knr/ppp3pp/2np4/8/4P3/2Q2N2/P4PPP/RNB2RK1 w - - 1 12"
        val moves = "f1e1 d6d5"
        val ranking = 777
        val database = PuzzleDatabase.LICHESS
        val r = app.client.put("/api/puzzle/as-admin") { putPuzzle(fen, moves, ranking, database); withToken(0) }
        r.status.isOk()
        val id = r.jsonPath<Int>("$.id")!!
        val p = getPuzzle(id)
        assertEquals(fen, p?.fen)
        assertEquals(moves, p?.moves)
        assertEquals(database, p?.database)
        assertEquals(ranking, p?.ranking)
    }

    @Test
    fun `putPuzzleAsAdminEndpoint returns OK and expected data`() = testSuspend {
        setupUser()
        setupAdmin()
        val fen = "rnbqkb1r/ppp1pppp/5n2/8/2PP4/5N2/PP3PPP/RNBQKB1R b KQkq - 2 5"
        val moves = "e7e5 d4d5"
        val ranking = 505
        val database = PuzzleDatabase.USER
        val r = app.client.put("/api/puzzle/as-admin") { putPuzzle(fen, moves, ranking, database); withToken(0) }
        r.status.isOk()
        r.jsonPath("$.fen", fen)
        r.jsonPath("$.moves", moves)
        r.jsonPath("$.ranking", ranking)
        r.jsonPath("$.database", database.name)
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns FORBID if user is not admin`() = testSuspend {
        setupUser()
        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0) }.status.isForbid()
    }

    private fun HttpRequestBuilder.themeNames(vararg names: String = arrayOf("mateIn1")) {
        jsonBody("themeNames" to names)
    }

    private fun setupThemes() = transaction {
        Themes.insert {
            it[id] = 0
            it[name] = "mate"
        }
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns BAD_REQUEST if is admin but puzzle does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupThemes()
        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0); themeNames("mate") }.status.isBadRequest()
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns BAD_REQUEST if is admin and puzzle exists but theme does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0); themeNames("mate") }.status.isBadRequest()
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns NO_CONTENT if admin, puzzle and theme name exists`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()
        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0); themeNames("mate") }.status.isNoContent()
    }

    private fun isPuzzleThemeExist(puzzleId: Int, themeId: Int): Boolean = transaction{
        PuzzleThemes.select {
            (PuzzleThemes.puzzleId eq puzzleId) and (PuzzleThemes.themeId eq themeId)
        }.count() > 0
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns NO_CONTENT and makes PuzzleTheme record in database`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()
        assertFalse { isPuzzleThemeExist(0, 0) }
        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0); themeNames("mate") }.status.isNoContent()
        assertTrue { isPuzzleThemeExist(0, 0) }
    }

    @Test
    fun `putThemeNamesAsAdminEndpoint returns BAD_REQUEST if theme already assigned`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()

        transaction {
            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 0
            }
        }

        app.client.put("/api/puzzle/theme/by-names/as-admin/0") {withToken(0); themeNames("mate") }.status.isBadRequest()
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns FORBIDDEN if user is not an admin`() = testSuspend {
        setupUser()
        app.client.delete("/api/puzzle/theme/by-ids/as-admin/0"){withToken(0)}.status.isForbid()
    }

    private fun HttpRequestBuilder.themeIds(vararg ids: Int = intArrayOf(0)) {
        jsonBody("themeIds" to ids)
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and body equals to 0 if puzzle does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds() }
        r.status.isOk()
        r.jsonPath("$", 0)
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK if admin and puzzle does exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()
        app.client.delete("/api/puzzle/theme/by-ids/as-admin/0"){withToken(0); themeIds()}.status.isOk()
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and body equals to 0 if puzzle exists but theme is not exists`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds(5) }
        r.status.isOk()
        r.jsonPath("$", 0)
    }

    private fun assignTheme(puzzleId: Int = 0, themeId: Int = 0) {
        transaction {
            PuzzleThemes.insert {
                it[PuzzleThemes.puzzleId] = puzzleId
                it[PuzzleThemes.themeId] = themeId
            }
        }
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and expected body if puzzle and theme exists`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()
        assignTheme()

        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds(0) }
        r.status.isOk()
        r.jsonPath("$", 1)
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and deletes PuzzleTheme from database`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        setupThemes()
        assignTheme()

        assertTrue { isPuzzleThemeExist(0, 0) }

        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds(0) }
        r.status.isOk()

        assertFalse { isPuzzleThemeExist(0, 0) }
    }

    private fun setupRandomThemesForPuzzle(puzzleId: Int = 0, rng: IntRange = 0..10) = transaction {
        rng.forEach {index ->
            Themes.insert {
                it[id] = index
                it[name] = UUID.randomUUID().toString().take(32)
            }

            PuzzleThemes.insert {
                it[PuzzleThemes.puzzleId] = puzzleId
                it[themeId] = index
            }
        }
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and deletes many puzzleThemes from database`() = testSuspend {
        val range = 0..20

        val ids = range.map { it }.toTypedArray().toIntArray()

        setupUser()
        setupAdmin()
        setupPuzzle()
        setupRandomThemesForPuzzle(puzzleId = 0, rng = range)

        range.forEach {i ->
            assertTrue { isPuzzleThemeExist(0, i) }
        }

        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds(*ids) }
        r.status.isOk()

        range.forEach {i ->
            assertFalse { isPuzzleThemeExist(0, i) }
        }
    }

    private fun isThemeById(id: Int): Boolean = transaction {
        Themes.select { Themes.id eq id }.count() > 0
    }

    @Test
    fun `deleteThemeIdsAsAdminEndpoint returns OK and deletes PuzzleTheme but not Themes`() = testSuspend {
        val range = 0..20

        val ids = range.map { it }.toTypedArray().toIntArray()

        setupUser()
        setupAdmin()
        setupPuzzle()
        setupRandomThemesForPuzzle(puzzleId = 0, rng = range)

        range.forEach {i ->
            assertTrue { isPuzzleThemeExist(0, i) }
            assertTrue { isThemeById(i) }
        }

        val r = app.client.delete("/api/puzzle/theme/by-ids/as-admin/0") { withToken(0); themeIds(*ids) }
        r.status.isOk()

        range.forEach {i ->
            assertFalse { isPuzzleThemeExist(0, i) }
            assertTrue { isThemeById(i) }
        }
    }

    @Test
    fun `updateRankingAsAdminEndpoint returns FORBID if no authentication`() = testSuspend {
        app.client.post("/api/puzzle/ranking/as-admin/0/3000").status.isForbid()
    }

    @Test
    fun `updateRankingAsAdminEndpoint returns FORBID if user, not admin`() = testSuspend {
        setupUser()
        app.client.post("/api/puzzle/ranking/as-admin/0/3000"){withToken(0)}.status.isForbid()
    }

    @Test
    fun `updateRankingAsAdminEndpoint returns BAD_REQUEST if admin, but puzzle does not exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/puzzle/ranking/as-admin/0/3000"){withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `updateRankingAsAdminEndpoint returns NO_CONTENT if admin and puzzle does exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        app.client.post("/api/puzzle/ranking/as-admin/0/3000"){withToken(0)}.status.isNoContent()
    }

    @Test
    fun `updateRankingAsAdminEndpoint returns NO_CONTENT and changes Puzzle ranking`() = testSuspend {
        setupUser()
        setupAdmin()
        setupPuzzle()
        app.client.post("/api/puzzle/ranking/as-admin/0/3000"){withToken(0)}.status.isNoContent()
        assertEquals(3000, getPuzzle(0)?.ranking)
    }
}