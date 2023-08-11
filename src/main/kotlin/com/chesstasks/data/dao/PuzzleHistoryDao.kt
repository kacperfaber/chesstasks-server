package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleHistoryDto
import com.chesstasks.data.dto.PuzzleHistoryItems
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
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

    suspend fun insert(userId: Int, puzzleId: Int, moves: String, success: Boolean): PuzzleHistoryDto? = dbQuery {
        PuzzleHistoryItems
            .insert {
                it[PuzzleHistoryItems.userId] = userId
                it[PuzzleHistoryItems.puzzleId] = puzzleId
                it[PuzzleHistoryItems.moves] = moves
                it[PuzzleHistoryItems.success] = success
            }.resultedValues?.map { PuzzleHistoryDto.from(it) }?.firstOrNull()
    }
}