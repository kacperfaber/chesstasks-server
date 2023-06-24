package com.chesstasks.game.puzzle

class PuzzleState(val puzzleId: Int, val fen: String, val computerMoves: List<BaseMove>, val userMoves: List<Move>) {
    var index = 1
}

open class BaseMove(val move: String)

enum class MoveSolveState(val value: String) {
    Ok("ok"),
    Wrong("wrong")
}

class Move(move: String, val solve: MoveSolveState?) : BaseMove(move)