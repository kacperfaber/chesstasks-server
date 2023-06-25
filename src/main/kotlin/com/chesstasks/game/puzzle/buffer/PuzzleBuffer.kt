package com.chesstasks.game.puzzle.buffer

import com.chesstasks.data.dto.PuzzleDto

data class PuzzleBuffer(val userId: Int, val settings: Settings) {
    internal var puzzles: List<PuzzleDto> = mutableListOf()

    data class Settings(val rankingOffset: Int)
}