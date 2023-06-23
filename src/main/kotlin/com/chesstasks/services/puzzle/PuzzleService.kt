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

    suspend fun deletePuzzle(id: Int, authenticatedUserId: Int): Boolean {
        return puzzleDao.deleteByIdAndAuthenticatedUserId(id, authenticatedUserId)
    }

    suspend fun deletePuzzle(id: Int): Boolean {
        return puzzleDao.deleteById(id)
    }

    suspend fun getAllByThemeName(themeName: String, skip: Long): List<PuzzleDto> =
        puzzleDao.getAllByThemeName(themeName, DEFAULT_LIMIT, skip)

    suspend fun getAllByOpeningEco(openingEco: String, skip: Long): List<PuzzleDto> {
        return puzzleDao.getAllByOpeningEco(openingEco, DEFAULT_LIMIT, skip)
    }

    suspend fun getAllByOpeningId(openingId: Int, skip: Long): List<PuzzleDto> {
        return puzzleDao.getAllByOpeningId(openingId, DEFAULT_LIMIT, skip)
    }

    suspend fun getRandomByRankingRange(min: Int, max: Int): List<PuzzleDto> {
        return puzzleDao.getRandomByRankingRange(min, max, DEFAULT_LIMIT, 0L)
    }
}