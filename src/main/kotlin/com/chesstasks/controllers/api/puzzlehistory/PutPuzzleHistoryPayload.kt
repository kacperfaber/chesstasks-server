package com.chesstasks.controllers.api.puzzlehistory

data class PutPuzzleHistoryPayload(val puzzleId: Int, val moves: String, val success: Boolean)