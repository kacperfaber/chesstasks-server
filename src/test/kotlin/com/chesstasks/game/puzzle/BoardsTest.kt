package com.chesstasks.game.puzzle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class BoardsTest {
    @Test
    fun `createBoard does not throw when FEN and firstMove is valid`() {
        assertDoesNotThrow { Boards.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e3") }
    }

    @Test
    fun `createBoard throws when FEN is invalid`() {
        assertThrows<Exception> { Boards.createBoard("invalidfen", "e2e4") }
    }

    @Test
    fun `createBoard returns expected Board-fen value`() {
        val res = Boards.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e3")
        assertEquals("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR b KQkq - 0 1", res.fen)
    }
}