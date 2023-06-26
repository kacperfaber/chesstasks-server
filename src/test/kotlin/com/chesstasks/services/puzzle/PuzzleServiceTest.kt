package com.chesstasks.services.puzzle

import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.Puzzles
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseTest
import testutils.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PuzzleServiceTest : BaseTest() {
    @Inject
    lateinit var puzzleService: PuzzleService

    companion object {
        const val initialFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    private fun setupRandomPuzzles(iter: Int, ranking: Int) = transaction {
        repeat(iter) {
            Puzzles.insert {
                it[fen] = initialFEN
                it[moves] = "e2e4 e7e5 d2d4 d7d5"
                it[database] = PuzzleDatabase.LICHESS
                it[Puzzles.ranking] = ranking
            }
        }
    }

    @Test
    fun `getListBySearchCriteria returns expected list size when DEFAULT_LIMIT used`() = testSuspend {
        setupRandomPuzzles(500, 1500)
        val searchCriteria = PuzzleService.SearchCriteria(1500, null, null, null)
        assertEquals(50, puzzleService.getListBySearchCriteria(searchCriteria, skip = 0L).size)
    }

    @Test
    fun `getListBySearchCriteria returns expected list size when limit is bigger than database size`() = testSuspend {
        setupRandomPuzzles(500, 1500)
        val searchCriteria = PuzzleService.SearchCriteria(1500, null, null, null)
        assertEquals(500, puzzleService.getListBySearchCriteria(searchCriteria, limit = 1000, skip = 0L).size)
    }

    @Test
    fun `getListBySearchCriteria returns only items matching to criteria`() = testSuspend{
        setupRandomPuzzles(10, 1500)
        setupRandomPuzzles(10, 500)
        val searchCriteria = PuzzleService.SearchCriteria(1500, null, null, null)
        val res = puzzleService.getListBySearchCriteria(searchCriteria, limit = 1000, skip = 0L)
        assertEquals(10, res.size)
        assertTrue { res.all { it.ranking == 1500 } }
    }
}