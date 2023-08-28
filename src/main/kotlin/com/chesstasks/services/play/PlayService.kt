package com.chesstasks.services.play

import com.chesstasks.services.puzzle.history.PuzzleHistoryService
import com.chesstasks.services.training.ranking.TrainingRankingService
import org.koin.core.annotation.Single

@Single
class PlayService(
    private val puzzleHistoryService: PuzzleHistoryService,
    private val rankingService: TrainingRankingService
) {
    class SubmitResult(val applied: Boolean, val ranking: Int?, val rankingDiff: Int?)

    /**
     * Will submit puzzle and returns new ranking,
     * if puzzle was never submitted.
     */
    suspend fun submitPuzzle(puzzleId: Int, currentUserId: Int, moves: String, success: Boolean): SubmitResult? {
        if (puzzleHistoryService.getByUserIdAndPuzzleId(currentUserId, puzzleId) != null)
            return SubmitResult(applied = false, null, null)

        val (ranking, rankingDiff) = rankingService.tryUpdateRanking(currentUserId, puzzleId, success) ?: return null

        puzzleHistoryService.submitPuzzleHistory(currentUserId, puzzleId, moves, success)

        return SubmitResult(applied = true, ranking, rankingDiff)
    }
}