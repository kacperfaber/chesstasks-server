package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleThemes
import com.chesstasks.data.dto.ThemeDto
import com.chesstasks.data.dto.Themes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single
class ThemeDao {
    suspend fun getThemeId(themeName: String): Int? = dbQuery {
        Themes.select { Themes.name eq themeName }.map { it[Themes.id] }.singleOrNull()
    }

    suspend fun insertTheme(name: String): Int = dbQuery {
        Themes.insert {
            it[Themes.name] = name
        } get Themes.id
    }

    suspend fun getThemes() = dbQuery {
        Themes.selectAll().map { row -> ThemeDto.from(row) }
    }
}