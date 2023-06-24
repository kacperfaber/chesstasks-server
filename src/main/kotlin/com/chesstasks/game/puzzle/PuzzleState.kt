package com.chesstasks.game.puzzle

class PuzzleState(val puzzleId: Int, val fen: String, val computerMoves: List<BaseMove>, val userMoves: List<Move>) {
    var index = 0

    val firstMove: String
        get() = computerMoves[0].move

    val userMove: Move
        get() = userMoves[index]

    val computerMove: BaseMove
        get() = computerMoves[index]

    fun submit(state: MoveSolveState) {
        userMove.solve = state
        index += 1
    }

    enum class FinishState(val value: String) {
        Solved("solved"),
        Wrong("wrong")
    }

    val finishState: FinishState?
        get() {
            if (userMoves.all { it.solve == MoveSolveState.Ok }) return FinishState.Solved
            if (userMoves.any { it.solve == MoveSolveState.Wrong }) return FinishState.Wrong
            return null
        }

}

open class BaseMove(val move: String)

enum class MoveSolveState(val value: String) {
    Ok("ok"),
    Wrong("wrong")
}

class Move(move: String, var solve: MoveSolveState?) : BaseMove(move)