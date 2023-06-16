package com.chesstasks.data.dao

import com.chesstasks.data.dto.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PuzzleDaoTest : BaseTest() {
    private fun setupPuzzle() {
        transaction {
            Puzzles.insert {
                it[id] = 0
                it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                it[moves] = "d3d4"
                it[ranking] = 1400
                it[database] = PuzzleDatabase.LICHESS
            }
        }
    }

    private fun setupPuzzleWithOwner(database: PuzzleDatabase = PuzzleDatabase.LICHESS) {
        transaction {
            Users.insert {
                it[id] = 0
                it[username] = "kacperfaber"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }

            Puzzles.insert {
                it[id] = 0
                it[ownerId] = 0
                it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                it[moves] = "d3d4"
                it[ranking] = 1400
                it[Puzzles.database] = database
            }
        }
    }

    private fun setupThemes() {
        transaction {
            Themes.insert {
                it[name] = "mate"
                it[Themes.id] = 0
            }

            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 0
            }
        }
    }

    @Test
    fun `getById does not throw when DATA exists`() = testSuspend {
        setupPuzzle()
        val puzzleDao = getInstance<PuzzleDao>()
        assertDoesNotThrow { puzzleDao.getById(0) }
    }

    @Test
    fun `getById does not throw DATA doesnt exist`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        assertDoesNotThrow { puzzleDao.getById(0) }
    }

    @Test
    fun `getById returns NOT NULL when DATA exists`() = testSuspend {
        setupPuzzle()
        val puzzleDao = getInstance<PuzzleDao>()
        assertNotNull(puzzleDao.getById(0))
    }

    @Test
    fun `getById returns NULL when DATA doesnt exists`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        assertNull(puzzleDao.getById(0))
    }

    @Test
    fun `getById returns expected length of list of themes`() = testSuspend {
        setupPuzzle()
        setupThemes()
        val puzzleDao = getInstance<PuzzleDao>()
        assertEquals(1, puzzleDao.getById(0)?.themes?.count())
    }

    @Test
    fun `getById returns expected list of themes`() = testSuspend {
        setupPuzzle()
        setupThemes()
        val puzzleDao = getInstance<PuzzleDao>()
        val res = puzzleDao.getById(0)
        val themes = res!!.themes
        assertEquals("mate", themes[0])
    }

    @Test
    fun `getById returns empty list if no themes assigned`() = testSuspend {
        setupPuzzle()
        val puzzleDao = getInstance<PuzzleDao>()
        assertEquals(0, puzzleDao.getById(0)!!.themes.count())
    }

    @Test
    fun `getById returns expected ID`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzle()
        val res = puzzleDao.getById(0)
        assertEquals(0, res?.id)
    }

    @Test
    fun `getById returns expected FEN and MOVES`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzle()
        val res = puzzleDao.getById(0)
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", res?.fen)
        assertEquals("d3d4", res?.moves)
    }

    @Test
    fun `getById returns not null owner when expected`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner()
        val res = puzzleDao.getById(0)
        assertNotNull(res?.owner)
    }

    @Test
    fun `getById returns not null ownerId when expected`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner()
        val res = puzzleDao.getById(0)
        assertNotNull(res?.ownerId)
    }

    @Test
    fun `getById returns expected ownerId`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner()
        val res = puzzleDao.getById(0)
        assertEquals(0, res?.ownerId)
    }

    @Test
    fun `getById returns expected owner instance`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner()
        val res = puzzleDao.getById(0)?.owner
        assertEquals(0, res?.id)
        assertEquals("kacperfaber", res?.username)
        assertEquals("kacperf1234@gmail.com", res?.emailAddress)
        assertEquals("HelloWorld123", res?.passwordHash)
    }

    @Test
    fun `getById returns expected database - LICHESS`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner(PuzzleDatabase.LICHESS)
        val res = puzzleDao.getById(0)
        assertEquals(PuzzleDatabase.LICHESS, res?.database)
    }

    @Test
    fun `getById returns expected database - USER`() = testSuspend {
        val puzzleDao = getInstance<PuzzleDao>()
        setupPuzzleWithOwner(PuzzleDatabase.USER)
        val res = puzzleDao.getById(0)
        assertEquals(PuzzleDatabase.USER, res?.database)
    }
}