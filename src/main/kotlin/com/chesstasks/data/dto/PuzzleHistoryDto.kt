package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object PuzzleHistoryItems : BaseTable("puzzle_history_items") {
    val puzzleId = integer("puzzle_id").references(Puzzles.id)
    val userId = integer("user_id").references(Users.id)
}

class PuzzleHistoryDto(id: Int, val userId: Int, val puzzleId: Int, createdAt: Long) : BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): PuzzleHistoryDto = PuzzleHistoryDto(
            r[PuzzleHistoryItems.id],
            r[PuzzleHistoryItems.userId],
            r[PuzzleHistoryItems.puzzleId],
            r[PuzzleHistoryItems.createdAt]
        )
    }
}