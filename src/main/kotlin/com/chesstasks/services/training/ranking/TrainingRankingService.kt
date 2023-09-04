package com.chesstasks.services.training.ranking

import com.chesstasks.data.dao.TrainingRankingDao
import com.chesstasks.data.dto.TrainingRankingDto
import com.chesstasks.services.puzzle.PuzzleService
import com.chesstasks.services.user.preferences.UserPreferencesService
import org.koin.core.annotation.Single

@Single
class TrainingRankingService(private val puzzleService: PuzzleService, private val userPreferencesService: UserPreferencesService, private val trainingRankingDao: TrainingRankingDao, private val rankingCalculator: RankingCalculator) {
    companion object {
        // TODO: Use properties.json or admin database.
        const val DEFAULT_RANKING = 1500
    }

    data class RankingUpdated(val ranking: Int, val rankingDiff: Int)

    suspend fun getByUserId(currentUserId: Int, userId: Int): TrainingRankingDto? {
        if (userPreferencesService.checkAccessToSeeStatistics(userId, currentUserId)) {
            return trainingRankingDao.getByUserId(userId) ?: trainingRankingDao.insertValues(userId, DEFAULT_RANKING)!!
        }
        return null
    }

    suspend fun getByUserId(userId: Int): TrainingRankingDto {

        return trainingRankingDao.getByUserId(userId) ?: trainingRankingDao.insertValues(userId, DEFAULT_RANKING)!!
    }

    suspend fun insertValues(userId: Int, ranking: Int): TrainingRankingDto? =
        trainingRankingDao.insertValues(userId, ranking)

    suspend fun updateRanking(userId: Int, ranking: Int): Boolean = trainingRankingDao.updateRanking(userId, ranking)

    suspend fun updateRanking(userId: Int, currentUserRanking: Int, puzzleRanking: Int, success: Boolean): Int {
        val newRanking = rankingCalculator.getNewRanking(currentUserRanking, puzzleRanking, success)
        updateRanking(userId, newRanking)
        return newRanking
    }

    suspend fun tryUpdateRanking(userId: Int, puzzleId: Int, success: Boolean): RankingUpdated? {
        val currentUserRanking = getByUserId(userId).ranking
        val puzzleRanking = puzzleService.getPuzzleRanking(puzzleId) ?: return null
        val newRanking = updateRanking(userId, currentUserRanking, puzzleRanking, success)
        return RankingUpdated(newRanking, rankingDiff = newRanking - currentUserRanking)
    }

    suspend fun setupDefault(userId: Int) {
        trainingRankingDao.insertValues(userId, DEFAULT_RANKING)
    }
}