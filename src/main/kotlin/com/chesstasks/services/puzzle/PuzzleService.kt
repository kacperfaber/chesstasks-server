package com.chesstasks.services.puzzle

import com.chesstasks.data.dao.PuzzleDao
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleDto
import org.koin.core.annotation.Single

@Single
class PuzzleService(private val puzzleDao: PuzzleDao) {
    suspend fun getById(id: Int): PuzzleDto? = puzzleDao.getById(id)

    suspend fun getAllByDatabase(database: PuzzleDatabase, limit: Int = 50, skip: Long = 0L): List<PuzzleDto> {
        return puzzleDao.getAllByDatabase(database, limit, skip)
    }

    suspend fun getAllByOwner(ownerId: Int, limit: Int, skip: Long): List<PuzzleDto> {
        return puzzleDao.getByOwnerId(ownerId, limit, skip)
    }
}