package com.chesstasks.controllers.puzzle.user

import com.chesstasks.requestvalidation.base.InsertPuzzlePayload

class InsertUserPuzzlePayload(fen: String, moves: String, val ranking: Int) : InsertPuzzlePayload(fen, moves)