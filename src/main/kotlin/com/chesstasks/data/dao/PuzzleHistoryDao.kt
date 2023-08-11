package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleHistoryDto
import com.chesstasks.data.dto.PuzzleHistoryItems
import org.jetbrains.exposed.sql.*
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

    suspend fun getTotalSolved(userId: Int): Long = dbQuery {
        PuzzleHistoryItems
            .select { (PuzzleHistoryItems.userId eq userId) and (PuzzleHistoryItems.success eq true) }
            .count()
    }

    suspend fun getTotalFails(userId: Int): Long = dbQuery {
        PuzzleHistoryItems
            .select { (PuzzleHistoryItems.userId eq userId) and (PuzzleHistoryItems.success eq false)}
            .count()
    }
}