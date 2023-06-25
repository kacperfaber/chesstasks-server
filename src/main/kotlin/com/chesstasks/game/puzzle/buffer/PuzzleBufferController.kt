package com.chesstasks.game.puzzle.buffer

import com.chesstasks.data.dto.PuzzleDto
import com.chesstasks.services.puzzle.PuzzleService
import org.koin.core.annotation.Single

@Single
class PuzzleBufferController(private val puzzleService: PuzzleService) {
    fun createBuffer(userId: Int, settings: PuzzleBuffer.Settings): PuzzleBuffer {
        return PuzzleBuffer(userId, settings)
    }

    private suspend fun refresh(puzzleBuffer: PuzzleBuffer) {
        puzzleBuffer.puzzles = puzzleService.getRandomByRankingRange(
            puzzleBuffer.settings.rankingMin,
            puzzleBuffer.settings.rankingMax
        )
    }

    suspend fun getPuzzle(puzzleBuffer: PuzzleBuffer): PuzzleDto {
        val puzzle = puzzleBuffer.puzzles.firstOrNull()
        if (puzzle != null) return puzzle
        refresh(puzzleBuffer)
        return puzzleBuffer.puzzles.first()
    }
}