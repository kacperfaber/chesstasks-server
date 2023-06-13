package com.chesstasks.services.puzzle.history

import com.chesstasks.data.dao.PuzzleHistoryItemDao
import com.chesstasks.data.dto.PuzzleHistoryItemDto
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryService(private val puzzleHistoryItemDao: PuzzleHistoryItemDao) {

    suspend fun getById(id: Int): PuzzleHistoryItemDto? = puzzleHistoryItemDao.getById(id)

    suspend fun getByIdAndAuthenticatedUser(id: Int, authenticatedUserId: Int) : PuzzleHistoryItemDto? = puzzleHistoryItemDao.getByIdAndAuthenticatedUser(id, authenticatedUserId)

    companion object {
        private const val DEFAULT_LIMIT = 50
    }
}