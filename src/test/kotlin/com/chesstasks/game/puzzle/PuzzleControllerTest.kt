package com.chesstasks.game.puzzle

import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import testutils.Inject
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull


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

    private fun puzzleState(fen: String, moves: List<String>): PuzzleState {
        return puzzleController.createPuzzleState(0, fen, moves)
    }

    @Test
    fun `makeMove does not throw`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        assertDoesNotThrow { puzzleController.makeMove(state, "e7e5") }
    }

    @Test
    fun `makeMove returns MoveSolveState-Wrong when move is wrong`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        val r = puzzleController.makeMove(state, "d2d4")
        assertEquals(MoveSolveState.Wrong, r)
    }

    @Test
    fun `makeMove returns MoveSolveState-Ok when move is ok`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        val r = puzzleController.makeMove(state, "e7e5")
        assertEquals(MoveSolveState.Ok, r)
    }

    @Test
    fun `makeMove makes finishState=FinishState-Wrong and returns MoveSolveState-Wrong when move is wrong`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        assertNull(state.finishState)
        val r = puzzleController.makeMove(state, "d2d4")
        assertEquals(MoveSolveState.Wrong, r)
        assertEquals(PuzzleState.FinishState.Wrong, state.finishState)
    }

    @Test
    fun `makeMove keeps finishState=null and returns MoveSolveState-Ok when move is ok and its not last`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5", "d2d4", "d7d5"))
        assertNull(state.finishState)
        val r = puzzleController.makeMove(state, "e7e5")
        assertEquals(MoveSolveState.Ok, r)
        assertNull(state.finishState)
    }

    @Test
    fun `makeMove makes finishState=FinishState-Solved and returns MoveSolveState-Ok when move is ok and it is last`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5"))
        assertNull(state.finishState)
        val r = puzzleController.makeMove(state, "e7e5")
        assertEquals(MoveSolveState.Ok, r)
        assertEquals(PuzzleState.FinishState.Solved, state.finishState)
    }

    @Test
    fun `makeMove makes index + 1`() = testSuspend {
        val state = puzzleState(initialFEN, listOf("e2e4", "e7e5"))
        val before = state.index
        val r = puzzleController.makeMove(state, "e7e5")
        assertEquals(before + 1, state.index)
    }


    companion object {
        const val initialFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

}