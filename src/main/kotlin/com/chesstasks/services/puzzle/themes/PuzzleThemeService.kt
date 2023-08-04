package com.chesstasks.services.puzzle.themes

import com.chesstasks.data.dao.PuzzleThemeDao
import com.chesstasks.data.dao.ThemeDao
import com.chesstasks.data.dto.ThemeDto
import org.koin.core.annotation.Single

@Single
class PuzzleThemeService(private val themeDao: ThemeDao, private val puzzleThemeDao: PuzzleThemeDao) {
    suspend fun getThemesIds(themeNames: List<String>): List<Int> {
        return themeNames.map { name -> themeDao.getThemeId(name)!! } // TODO: Throw some kinda exception to inform user, that only admins can add themes.
    }

    suspend fun isThemeExists(themeName: String): Boolean {
        return themeDao.getThemeId(themeName) != null
    }

    suspend fun getThemeId(themeName: String): Int? {
        return themeDao.getThemeId(themeName)
    }

    suspend fun tryAssignThemes(puzzleId: Int, themeNames: List<String>) {
        getThemesIds(themeNames).forEach {themeId ->
            puzzleThemeDao.insertPuzzleTheme(puzzleId, themeId)
        }
    }

    suspend fun deleteAllThemesByPuzzleId(puzzleId: Int): Boolean = puzzleThemeDao.deleteAllByPuzzleId(puzzleId) > 0

    suspend fun getThemes(): List<ThemeDto> {
        return themeDao.getThemes()
    }
}