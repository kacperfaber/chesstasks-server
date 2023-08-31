package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.LoginCounterDto
import com.chesstasks.data.dto.LoginCounters
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class LoginCounterDao {
    suspend fun insertEmpty(userId: Int): LoginCounterDto = dbQuery {
        LoginCounters.insert {
            it[LoginCounters.userId] = userId
            it[LoginCounters.counter] = 0
        }
            .resultedValues
            ?.map(LoginCounterDto::from)
            ?.singleOrNull() !!
    }

    suspend fun getCounterValue(userId: Int): Int? = dbQuery {
        LoginCounters
            .select { LoginCounters.userId eq userId }
            .map {it[LoginCounters.counter]}
            .singleOrNull()
    }

    suspend fun resetCounterValue(userId: Int): Boolean = dbQuery {
        LoginCounters
            .update(where = {LoginCounters.userId eq userId}) {
                it[counter] = 0
            } == 1
    }
}