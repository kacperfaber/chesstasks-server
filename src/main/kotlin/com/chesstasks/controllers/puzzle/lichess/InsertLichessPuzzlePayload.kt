package com.chesstasks.controllers.puzzle.lichess

data class InsertLichessPuzzlePayload(val id: String, val fen: String, val moves: String, val ranking: Int)