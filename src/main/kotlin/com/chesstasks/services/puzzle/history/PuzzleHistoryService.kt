package com.chesstasks.services.puzzle.history

import com.chesstasks.data.dao.PuzzleHistoryDao
import com.chesstasks.data.dto.PuzzleHistoryDto
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryService(private val puzzleHistoryDao: PuzzleHistoryDao) {

    suspend fun getAll(userId: Int, limit: Int = DEFAULT_LIMIT, skip: Long = 0): List<PuzzleHistoryDto> =
        puzzleHistoryDao.getAll(userId, limit, skip)

    companion object {
        const val DEFAULT_LIMIT = 50
    }

}