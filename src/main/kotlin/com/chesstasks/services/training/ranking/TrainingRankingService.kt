package com.chesstasks.services.training.ranking

import com.chesstasks.data.dao.TrainingRankingDao
import com.chesstasks.data.dto.TrainingRankingDto
import org.koin.core.annotation.Single

@Single
class TrainingRankingService(private val trainingRankingDao: TrainingRankingDao, private val rankingCalculator: RankingCalculator) {
    companion object {
        // TODO: Use properties.json or admin database.
        const val DEFAULT_RANKING = 1500
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
}