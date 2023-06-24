package com.chesstasks.game.puzzle

import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import testutils.Inject
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals


class PuzzleControllerTest : BaseTest() {

    @Inject
    lateinit var puzzleController: PuzzleController

    @Test
    fun `createPuzzleState does not throw`() = testSuspend {
        assertDoesNotThrow { puzzleController.createPuzzleState(0, "", listOf("e2e4", "e7e5")) }
    }

    @Test
    fun `createPuzzleState returns expected puzzleId value`() = testSuspend {
        val id = Random.nextInt(0, 10000)
        assertEquals(id, puzzleController.createPuzzleState(id, "", listOf("e2e4", "e7e5")).puzzleId)
    }

    @Test
    fun `createPuzzleState returns expected fen value`() = testSuspend {
        val fen = UUID.randomUUID().toString()
        assertEquals(fen, puzzleController.createPuzzleState(0, fen, listOf("e2e4", "e7e5")).fen)
    }

    @Test
    fun `createPuzzleState returns expected 'computerMoves' size`() = testSuspend {
        assertEquals(1, puzzleController.createPuzzleState(0,"", listOf("e2e4", "e7e5")).computerMoves.size)
    }

    @Test
    fun `createPuzzleState returns expected 'userMoves' size`() = testSuspend {
        assertEquals(1, puzzleController.createPuzzleState(0,"", listOf("e2e4", "e7e5")).userMoves.size)
    }

    @Test
    fun `createPuzzleState returns expected 'computerMoves' items`() = testSuspend {
        val res = puzzleController.createPuzzleState(0, "", listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        assertEquals("e2e4,d2d4", res.computerMoves.map { it.move }.joinToString(","))
    }

    @Test
    fun `createPuzzleState returns expected 'userMoves' items`() = testSuspend {
        val res = puzzleController.createPuzzleState(0, "", listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        assertEquals("e7e5,d7d5", res.userMoves.map { it.move }.joinToString(","))
    }

}