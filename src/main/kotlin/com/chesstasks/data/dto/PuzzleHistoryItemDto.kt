package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object PuzzleHistoryItems : BaseTable("puzzle_history_items") {
    /**
     * NULL, when it's solved
     * When it's not. Then last move is wrong.
     * */
    val moves = varchar("moves", 32).nullable()
    val puzzleId = integer("puzzle_id").references(Puzzles.id)
    val userId = integer("user_id").references(Users.id)
}

class PuzzleHistoryItemDto(
    id: Int,
    val moves: String?,
    val puzzleId: Int,
    val puzzle: PuzzleDto,
    val userId: Int,
    val user: UserDto,
    createdAt: Long
) : BaseDto(id, createdAt) {
    companion object {
        fun from(row: ResultRow): PuzzleHistoryItemDto {
            return PuzzleHistoryItemDto(
                row[PuzzleHistoryItems.id],
                row[PuzzleHistoryItems.moves],
                row[PuzzleHistoryItems.puzzleId],
                PuzzleDto.from(row),
                row[PuzzleHistoryItems.userId],
                UserDto.tryFrom(row)!!,
                row[PuzzleHistoryItems.createdAt]
            )
        }
    }
}