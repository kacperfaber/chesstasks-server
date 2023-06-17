package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.OpeningDto
import com.chesstasks.data.dto.Openings
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class OpeningDao {
    suspend fun getById(id: Int): OpeningDto? = dbQuery {
        Openings.select {Openings.id eq id}.map(OpeningDto::from).singleOrNull()
    }
}