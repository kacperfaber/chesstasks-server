package com.chesstasks.services.training.ranking

import com.chesstasks.data.dao.TrainingRankingDao
import com.chesstasks.data.dto.TrainingRankingDto
import org.koin.core.annotation.Single

@Single
class TrainingRankingService(private val trainingRankingDao: TrainingRankingDao) {
    suspend fun getRankingByUserId(userId: Int): Int? = trainingRankingDao.getRankingByUserId(userId)

    suspend fun getByUserId(userId: Int): TrainingRankingDto? = trainingRankingDao.getByUserId(userId)

    suspend fun insertValues(userId: Int, ranking: Int): TrainingRankingDto? =
        trainingRankingDao.insertValues(userId, ranking)

    suspend fun updateRanking(userId: Int, ranking: Int): Boolean = trainingRankingDao.updateRanking(userId, ranking)
}