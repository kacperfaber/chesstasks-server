package com.chesstasks.services.puzzle

import com.chesstasks.data.dao.PuzzleDao
import com.chesstasks.data.dto.*
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
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

    class SearchCriteria(val ranking: Int, private val rankingOffset: Int?, private val themeId: Int?, private val database: PuzzleDatabase?) {
        fun getWheres(): List<Op<Boolean>> {
            return listOfNotNull(
                if (themeId != null) PuzzleThemes.puzzleId eq themeId else null,
                (Puzzles.ranking greaterEq (ranking + (rankingOffset ?: 0)) - 100) and (Puzzles.ranking lessEq (ranking + (rankingOffset ?: 0)) + 100),
                if (database != null) Puzzles.database eq database else null
            )
        }
    }

    suspend fun getListBySearchCriteria(searchCriteria: SearchCriteria, limit: Int = DEFAULT_LIMIT, skip: Long): List<PuzzleDto> {
        return puzzleDao.getList(searchCriteria.getWheres(), limit, skip)
    }

    suspend fun getRandomListBySearchCriteria(searchCriteria: SearchCriteria, limit: Int = DEFAULT_LIMIT, skip: Long): List<PuzzleDto> {
        return puzzleDao.getRandomList(searchCriteria.getWheres(), limit, skip)
    }

    suspend fun searchPuzzles(criteria: PuzzleDao.SearchPuzzlesCriteria, limit: Int = DEFAULT_LIMIT): List<PuzzleDto> {
        return puzzleDao.searchPuzzles(criteria, limit)
    }

    suspend fun getPuzzleRanking(puzzleId: Int): Int? = puzzleDao.getPuzzleRanking(puzzleId)

    suspend fun insertPuzzleAsAdmin(fen: String, moves: String, ranking: Int, database: PuzzleDatabase): PuzzleDto? {
        return puzzleDao.insertPuzzleAsAdmin(fen, moves, ranking, database)
    }

    suspend fun updateRanking(puzzleId: Int, newRanking: Int): Boolean {
        return puzzleDao.updateRanking(puzzleId, newRanking)
    }
}