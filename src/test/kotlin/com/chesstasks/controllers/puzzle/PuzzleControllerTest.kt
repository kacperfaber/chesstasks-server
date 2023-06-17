package com.chesstasks.controllers.puzzle

import com.chesstasks.data.dto.*
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        idRange(r, 0..49, skip = 0)
    }

    @Test
    fun `byLichessDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        val r = app.client.get("/puzzle/all/by-database/lichess?skip=100") { withToken(0) }
        idRange(r, 100..149, skip = -100)
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
        idRange(r, 0..49, skip = 0)
    }

    @Test
    fun `byUserDbEndpoint returns items that we expect with SKIP`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.USER)
        val r = app.client.get("/puzzle/all/by-database/user?skip=100") { withToken(0) }
        idRange(r, 100..149, skip = -100)
    }

    @Test
    fun `deleteByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/puzzle/0").status.isForbid()
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
        app.client.delete("/puzzle/0") { withToken(1) }.status.isBadRequest()
    }

    @Test
    fun `deleteByIdEndpoint returns BAD_REQUEST if authenticated but resource does not exist`() = testSuspend {
        setupUser()
        app.client.delete("/puzzle/0") { withToken(0) }.status.isBadRequest()
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
        app.client.delete("/puzzle/0") { withToken(0) }.status.isNoContent()
    }

    @Test
    fun `deleteByIdEndpoint makes Puzzles table smaller by 1 and returns No_CONTENT`() = testSuspend {
        setupUser()
        setupPuzzle()
        val before = countPuzzles()
        app.client.delete("/puzzle/0") { withToken(0) }.status.isNoContent()
        val now = countPuzzles()
        assertEquals(before - 1, now)
    }

    @Test
    fun `deleteByIdEndpoint does not affect Puzzle table size when returns BAD_REQUEST`() = testSuspend {
        setupUser()
        val before = countPuzzles()
        app.client.delete("/puzzle/0") { withToken(0) }.status.isBadRequest()
        val now = countPuzzles()
        assertEquals(before, now)
    }

    @Test
    fun `deleteByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        val before = countPuzzles()
        app.client.delete("/puzzle/0").status.isForbid()
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
        app.client.delete("/puzzle/0") { withToken(0) }.status.isNoContent()
        assertNull(getPuzzle(0))
    }

    @Test
    fun `deleteByIdEndpoint doesnt deletes item we expect and returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        assertNotNull(getPuzzle(0))
        app.client.delete("/puzzle/0").status.isForbid()
        assertNotNull(getPuzzle(0))
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/puzzle/as-admin/0").status.isForbid()
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns FORBIDDEN if authenticated just as user`() = testSuspend {
        setupUser()
        setupPuzzle()
        app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isForbid()
    }

    @Test
    fun `deleteAsAdminByIdEndpoint returns BAD_REQUEST if authenticated as admin but resource does not exist`() =
        testSuspend {
            setupUser()
            setupAdmin()
            app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isBadRequest()
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and resource does exist`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()
            app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and deletes resource we expect`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()

            assertNotNull(getPuzzle(0))

            app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()

            assertNull(getPuzzle(0))
        }

    @Test
    fun `deleteAsAdminByIdEndpoint returns NO_CONTENT if authenticated as admin and makes Puzzle table smaller by 1`() =
        testSuspend {
            setupUser()
            setupPuzzle()
            setupAdmin()

            val before = countPuzzles()

            app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isNoContent()

            assertEquals(before - 1, countPuzzles())
        }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN`() = testSuspend {
        setupUser()
        setupPuzzle()
        setupAdmin()

        val before = countPuzzles()

        app.client.delete("/puzzle/as-admin/0").status.isForbid()

        assertEquals(before, countPuzzles())
    }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns FORBIDDEN - when it's just user`() =
        testSuspend {
            setupUser()
            setupPuzzle()

            val before = countPuzzles()

            app.client.delete("/puzzle/as-admin/0") { withToken(0) }.status.isForbid()

            assertEquals(before, countPuzzles())
        }

    @Test
    fun `deleteAsAdminByIdEndpoint does not affect Puzzle table size when returns BAD_REQUEST`() = testSuspend {
        setupUser()
        setupRandomPuzzle(500, PuzzleDatabase.LICHESS)
        setupAdmin()

        val bef = countPuzzles()
        app.client.delete("/puzzle/as-admin/-500") { withToken(0) }.status.isBadRequest()
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
        app.client.get("/puzzle/by-theme/mate").status.isForbid()
    }

    @Test
    fun `getAllByTheme returns OK auth`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupPuzzle()
        setupMatePuzzles()
        app.client.get("/puzzle/by-theme/mate") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getAllByTheme returns OK and expected items length`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupPuzzle()
        setupMatePuzzles()
        val r = app.client.get("/puzzle/by-theme/mate") { withToken(0) }
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
        val r = app.client.get("/puzzle/by-theme/mate") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 50)
    }

    @Test
    fun `getAllByTheme returns OK and maximum 50 items length USING SKIP`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupRandomMatePuzzle(500, 0)
        val r = app.client.get("/puzzle/by-theme/mate?skip=100") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 100)
        r.jsonPath("$[49].id", 149)
    }

    @Test
    fun `getAllByTheme returns OK and expected first and last item ID`() = testSuspend {
        setupUser()
        setupMateTheme()
        setupRandomMatePuzzle(500, 0)
        val r = app.client.get("/puzzle/by-theme/mate") { withToken(0) }
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
        repeat(iteration) {iter ->
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

        app.client.get("/puzzle/by-theme/mate"){withToken(0)}.jsonPath("$.length()", 37)
    }
}