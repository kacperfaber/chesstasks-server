package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleHistoryDto
import com.chesstasks.data.dto.PuzzleHistoryItems
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryDao {
    suspend fun getAll(userId: Int, limit: Int, skip: Long): List<PuzzleHistoryDto> = dbQuery {
        PuzzleHistoryItems
            .select { PuzzleHistoryItems.userId eq userId }
            .orderBy(PuzzleHistoryItems.createdAt, SortOrder.DESC)
            .limit(limit, skip)
            .map(PuzzleHistoryDto::from)
    }

    // TODO: Add something more...
}