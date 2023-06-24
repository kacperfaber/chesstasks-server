package com.chesstasks.game.puzzle

import org.koin.core.annotation.Single

@Single
class PuzzleController {

    internal fun generateMoveLists(moves: List<String>): Pair<List<BaseMove>, List<Move>> {
        fun isComputerMove(i: Int) = i % 2 == 0

        val computerMoves = mutableListOf<BaseMove>()
        val userMoves = mutableListOf<Move>()

        for ((i: Int, x: String) in moves.withIndex()) {
            if (isComputerMove(i)) computerMoves.add(BaseMove(x))
            else userMoves.add(Move(x, null))
        }

        return Pair(computerMoves, userMoves)
    }

    fun createPuzzleState(puzzleId: Int, fen: String, moves: List<String>): PuzzleState {
        val (computerMoves, userMoves) = generateMoveLists(moves)
        return PuzzleState(puzzleId, fen, computerMoves, userMoves)
    }

}