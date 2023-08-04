package com.chesstasks.data.dto

import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object PuzzleThemes : Table("puzzle_themes") {
    val themeId = integer("theme_id").references(Themes.id)
    val puzzleId = integer("puzzle_id").references(Puzzles.id)
    override val primaryKey: PrimaryKey = PrimaryKey(puzzleId, themeId)
}

object Themes : BaseTable("themes"){
    val name = varchar("name", 32).uniqueIndex()
}

class ThemeDto(val id: Int, val name: String) {
    companion object {
        fun from(row: ResultRow): ThemeDto {
            return ThemeDto(row[Themes.id], row[Themes.name])
        }
    }
}