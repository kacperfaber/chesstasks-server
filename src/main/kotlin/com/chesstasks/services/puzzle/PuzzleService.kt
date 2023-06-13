package com.chesstasks.services.puzzle

import com.chesstasks.data.dao.PuzzleDao
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleDto
import org.koin.core.annotation.Single

@Single
class PuzzleService(private val puzzleDao: PuzzleDao) {
    suspend fun getById(id: Int): PuzzleDto? = puzzleDao.getById(id)

    companion object {
        const val DEFAULT_LIMIT = 50
    }

    suspend fun getAllByDatabase(database: PuzzleDatabase, skip: Long = 0L): List<PuzzleDto> {
        return puzzleDao.getAllByDatabase(database, DEFAULT_LIMIT, skip)
    }

    suspend fun getAllByOwner(ownerId: Int, skip: Long): List<PuzzleDto> {
        return puzzleDao.getByOwnerId(ownerId, DEFAULT_LIMIT, skip)
    }
}