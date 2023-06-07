package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object UserPuzzles : BaseTable("user_puzzles") {
    val fen = varchar("fen", 128) // Chat said the longest possible is 86
    val moves = text("moves")
    val ownerId = integer("owner_id").references(Users.id)
    val confirmedAt = long("confirmed_at").nullable()
    val ranking = integer("ranking")
}

class UserPuzzleDto(id: Int, val fen: String, val moves: String, val ownerId: Int, val confirmedAt: Long?, createdAt: Long, val ranking: Int) : BaseDto(id, createdAt) {
    companion object {
        fun from(row: ResultRow): UserPuzzleDto {
            return UserPuzzleDto(
                row[UserPuzzles.id],
                row[UserPuzzles.fen],
                row[UserPuzzles.moves],
                ownerId = row[UserPuzzles.ownerId],
                confirmedAt = row[UserPuzzles.confirmedAt],
                row[UserPuzzles.createdAt],
                ranking = row[UserPuzzles.ranking]
            )
        }
    }
}