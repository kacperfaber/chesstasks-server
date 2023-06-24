package com.chesstasks.game.puzzle

import com.github.bhlangonijr.chesslib.Board
import java.util.*

object Boards {
    val boards = Collections.synchronizedMap(LinkedHashMap<PuzzleState, Board>())

    internal fun createBoard(fen: String, firstMove: String): Board {
        return Board().apply { loadFromFen(fen); doMove(firstMove) }
    }

    fun getBoard(state: PuzzleState): Board = boards.getOrPut(state) { createBoard(state.fen, state.firstMove) }
}