package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleHistoryItemDto
import com.chesstasks.data.dto.PuzzleHistoryItemDto.Companion.from
import com.chesstasks.data.dto.PuzzleHistoryItems
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class PuzzleHistoryItemDao {
    suspend fun getByIdAndAuthenticatedUser(id: Int, authenticatedUserId: Int): PuzzleHistoryItemDto? {
        return dbQuery {
            PuzzleHistoryItems
                .innerJoin(Users)
                .innerJoin(Puzzles)
                .select { (PuzzleHistoryItems.id eq id) and (PuzzleHistoryItems.userId eq authenticatedUserId) }
                .map(::from)
                .singleOrNull()
        }
    }

    suspend fun getById(id: Int): PuzzleHistoryItemDto? {
        return dbQuery {
            PuzzleHistoryItems
                .innerJoin(Users)
                .innerJoin(Puzzles)
                .select { PuzzleHistoryItems.id eq id }.map(::from).singleOrNull()
        }
    }

    suspend fun getAllByUserId(userId: Int, limit: Int, skip: Long): List<PuzzleHistoryItemDto> {
        return dbQuery {
            PuzzleHistoryItems
                .innerJoin(Users)
                .innerJoin(Puzzles)
                .select { PuzzleHistoryItems.userId eq userId }.limit(limit, skip).map(::from)
        }
    }

    suspend fun getByUserIdAndPuzzleId(userId: Int, puzzleId: Int, limit: Int, skip: Long): List<PuzzleHistoryItemDto> {
        return dbQuery {
            PuzzleHistoryItems
                .innerJoin(Users)
                .innerJoin(Puzzles)
                .select { (PuzzleHistoryItems.userId eq userId) and (PuzzleHistoryItems.puzzleId eq puzzleId) }
                .limit(limit, skip)
                .map(::from)
        }
    }

    suspend fun getByUserId(userId: Int, authenticatedUserId: Int, limit: Int, skip: Long): List<PuzzleHistoryItemDto> {
        return dbQuery {
            PuzzleHistoryItems
                .innerJoin(Users)
                .innerJoin(Puzzles)
                .select { (PuzzleHistoryItems.userId eq userId) }
                .limit(limit, skip)
                .map(::from)
        }
    }
}