package com.chesstasks.data.dto

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class UserPreferencesDao {
    suspend fun getHistoryVisibility(userId: Int): UserPuzzleHistoryVisibility? {
        return dbQuery {
            UserPreferences
                .slice(UserPreferences.userId, UserPreferences.historyVisibility)
                .select { UserPreferences.userId eq userId }
                .firstOrNull()
                ?.get(UserPreferences.historyVisibility)
        }
    }

    suspend fun insertValues(
        userId: Int,
        historyVisibility: UserPuzzleHistoryVisibility,
        statisticsVisibility: UserStatisticsVisibility
    ): Int {
        return dbQuery {
            UserPreferences.insert {
                it[UserPreferences.userId] = userId
                it[UserPreferences.historyVisibility] = historyVisibility
                it[UserPreferences.statisticsVisibility] = statisticsVisibility
            } get UserPreferences.id
        }
    }

    suspend fun setHistoryVisibility(userId: Int, value: UserPuzzleHistoryVisibility) = dbQuery {
        UserPreferences.update(where = { UserPreferences.userId eq userId }) {
            it[historyVisibility] = value
        } > 0
    }

    suspend fun getStatisticsVisibility(userId: Int) = dbQuery {
        UserPreferences
            .select { UserPreferences.userId eq userId }
            .map { it[UserPreferences.statisticsVisibility] }
            .firstOrNull()
    }

    suspend fun setStatisticsVisibility(userId: Int, value: UserStatisticsVisibility) = dbQuery {
        UserPreferences
            .update(
                where = {UserPreferences.userId eq userId},
                body = {it[statisticsVisibility] = value}
            ) > 0
    }
}