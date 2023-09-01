package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object PuzzleHistoryItems : BaseTable("puzzle_history_items") {
    val puzzleId = integer("puzzle_id").references(Puzzles.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val moves = varchar("moves", 255)
    val success = bool("success")
}

class PuzzleHistoryDto(id: Int, val userId: Int, val puzzleId: Int, val moves: String, val success: Boolean, createdAt: Long) : BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): PuzzleHistoryDto = PuzzleHistoryDto(
            r[PuzzleHistoryItems.id],
            r[PuzzleHistoryItems.userId],
            r[PuzzleHistoryItems.puzzleId],
            r[PuzzleHistoryItems.moves],
            r[PuzzleHistoryItems.success],
            r[PuzzleHistoryItems.createdAt]
        )
    }
}