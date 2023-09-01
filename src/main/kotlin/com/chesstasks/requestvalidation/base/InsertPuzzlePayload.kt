package com.chesstasks.requestvalidation.base

import com.chesstasks.data.dto.PuzzleDatabase

open class InsertPuzzlePayload(val fen: String, val moves: String, val ranking: Int, val database: PuzzleDatabase)