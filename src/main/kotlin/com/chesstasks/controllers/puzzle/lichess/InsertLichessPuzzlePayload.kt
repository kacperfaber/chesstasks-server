package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.requestvalidation.base.InsertPuzzlePayload

class InsertLichessPuzzlePayload(val id: String, fen: String, moves: String, val ranking: Int) : InsertPuzzlePayload(fen, moves)