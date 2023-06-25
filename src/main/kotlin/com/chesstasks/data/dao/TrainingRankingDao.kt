package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.TrainingRankingDto
import com.chesstasks.data.dto.TrainingRankings
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class TrainingRankingDao {
    suspend fun getRankingByUserId(userId: Int): Int? = dbQuery {
        TrainingRankings.select { TrainingRankings.userId eq userId }.firstOrNull()?.get(TrainingRankings.ranking)
    }

    suspend fun getByUserId(userId: Int): TrainingRankingDto? = dbQuery {
        TrainingRankings.select {TrainingRankings.userId eq userId}?.map(TrainingRankingDto::from)?.firstOrNull()
    }

    suspend fun insertValues(userId: Int, ranking: Int): TrainingRankingDto? = dbQuery {
        TrainingRankings.insert {
            it[TrainingRankings.userId] = userId
            it[TrainingRankings.ranking] = ranking
        }
            .resultedValues
            ?.map(TrainingRankingDto::from)
            ?.firstOrNull()
    }

    suspend fun updateRanking(userId: Int, ranking: Int): Boolean = dbQuery {
        TrainingRankings.update({ TrainingRankings.userId eq userId }) {
            it[TrainingRankings.ranking] = ranking
        } > 0
    }
}