package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

enum class PuzzleDatabase(val value: Int) {
    LICHESS(0),
    USER(1)
}

object Puzzles : BaseTable("puzzles") {
    /** NULL, when it's imported from Lichess or by administrator. */
    val ownerId = integer("owner_id").references(Users.id).nullable()

    val fen = varchar("fen", 128)
    val moves = varchar("moves", 256)
    val ranking = integer("ranking")
    val database = enumeration<PuzzleDatabase>("database")
}

class PuzzleDto(
    id: Int,
    @JsonIgnore val owner: UserDto?,
    val ownerId: Int?,
    val fen: String,
    val moves: String,
    val ranking: Int,
    val database: PuzzleDatabase,
    val themes: List<String>,
    createdAt: Long
) : BaseDto(id, createdAt) {
    companion object {
        fun from(row: ResultRow): PuzzleDto {
            val id = row[Puzzles.id]

            val themeIds = PuzzleThemes
                .select { PuzzleThemes.puzzleId eq id }
                .map { it[PuzzleThemes.themeId] }

            val themes = Themes.select {
                Themes.id inList themeIds
            }.map { it[Themes.name] }

            return PuzzleDto(
                id,
                UserDto.tryFrom(row),
                row[Puzzles.ownerId],
                row[Puzzles.fen],
                row[Puzzles.moves],
                row[Puzzles.ranking],
                row[Puzzles.database],
                themes,
                row[Puzzles.createdAt]
            )
        }
    }
}