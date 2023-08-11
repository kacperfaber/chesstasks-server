package com.chesstasks.services.statistics

import com.chesstasks.services.puzzle.history.PuzzleHistoryService
import com.chesstasks.services.user.preferences.UserPreferencesService
import org.koin.core.annotation.Single

@Single
class StatisticsService(
    private val userPreferencesService: UserPreferencesService,
    private val puzzleHistoryService: PuzzleHistoryService
) {
    suspend fun getSimpleStatisticsFor(userId: Int, authenticatedUserId: Int): SimpleUserStatistics? {
        if (userPreferencesService.checkAccessToSeeStatistics(userId, authenticatedUserId)) {
            return SimpleUserStatistics(
                totalSolved = puzzleHistoryService.getTotalSolved(userId),
                totalFails = puzzleHistoryService.getTotalFails(userId)
            )
        }
        return null
    }
}