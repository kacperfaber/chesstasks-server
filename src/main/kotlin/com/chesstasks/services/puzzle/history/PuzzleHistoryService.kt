package com.chesstasks.services.puzzle.history

import com.chesstasks.data.dao.PuzzleHistoryItemDao
import com.chesstasks.data.dto.PuzzleHistoryItemDto
import com.chesstasks.services.user.preferences.UserPreferencesService
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryService(
    private val puzzleHistoryItemDao: PuzzleHistoryItemDao,
    private val userPreferencesService: UserPreferencesService
) {

    suspend fun getById(id: Int): PuzzleHistoryItemDto? = puzzleHistoryItemDao.getById(id)

    suspend fun getByIdAndAuthenticatedUser(id: Int, authenticatedUserId: Int): PuzzleHistoryItemDto? =
        puzzleHistoryItemDao.getByIdAndAuthenticatedUser(id, authenticatedUserId)

    companion object {
        private const val DEFAULT_LIMIT = 50
    }

    suspend fun getByUserId(
        userId: Int,
        authenticatedUserId: Int,
        skip: Long = 0
    ): List<PuzzleHistoryItemDto>? {
        val isAccess = userPreferencesService.checkAccessToSeePuzzleHistory(userId, authenticatedUserId)
        return if (isAccess) puzzleHistoryItemDao.getByUserId(userId, authenticatedUserId, DEFAULT_LIMIT, skip) else null
    }
}