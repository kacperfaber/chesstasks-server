package com.chesstasks.services.puzzle.history

import com.chesstasks.data.dao.PuzzleHistoryDao
import com.chesstasks.data.dto.PuzzleHistoryDto
import com.chesstasks.services.user.preferences.UserPreferencesService
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryService(private val puzzleHistoryDao: PuzzleHistoryDao, private val userPreferencesService: UserPreferencesService) {
    companion object {
        const val DEFAULT_LIMIT = 50
    }

    suspend fun getAll(userId: Int, authenticatedUserId: Int, limit: Int = DEFAULT_LIMIT, skip: Long = 0): List<PuzzleHistoryDto>? {
        if (userPreferencesService.checkAccessToSeePuzzleHistory(userId, authenticatedUserId)) {
            return puzzleHistoryDao.getAll(userId, limit, skip)
        }
        return null
    }

    suspend fun submitPuzzleHistory(userId: Int, puzzleId: Int, moves: String, success: Boolean): PuzzleHistoryDto? {
        return puzzleHistoryDao.insert(userId, puzzleId, moves, success)
    }

    suspend fun getTotalSolved(userId: Int): Long {
        return puzzleHistoryDao.getTotalSolved(userId)
    }

    suspend fun getTotalFails(userId: Int): Long = puzzleHistoryDao.getTotalFails(userId)

}