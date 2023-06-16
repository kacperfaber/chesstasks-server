package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleThemes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.koin.core.annotation.Single

@Single
class PuzzleThemeDao {
    suspend fun insertPuzzleTheme(puzzleId: Int, themeId: Int): Unit = dbQuery {
        PuzzleThemes.insert {
            it[PuzzleThemes.themeId] = themeId
            it[PuzzleThemes.puzzleId] = puzzleId
        }
    }

    suspend fun deleteAllByPuzzleId(puzzleId: Int): Int = dbQuery {
        PuzzleThemes.deleteWhere { PuzzleThemes.puzzleId eq puzzleId }
    }
}