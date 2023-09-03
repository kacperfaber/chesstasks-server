package com.chesstasks.services.importpuzzle

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dao.ThemeDao
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.services.puzzle.themes.PuzzleThemeService
import org.jetbrains.exposed.sql.insert
import org.koin.core.annotation.Single
import java.io.File
import java.io.FileReader
import java.security.MessageDigest

@Single
class ImportPuzzleService(private val themeDao: ThemeDao, private val puzzleThemeService: PuzzleThemeService) {
    private suspend fun ensureThemes(themes: List<String>) {
        themes
            .filter { !themeDao.isThemeExist(it) }
            .forEach { themeDao.insertTheme(it) }
    }

    private fun getPuzzleId(row: PuzzleCsvRow): Int {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(row.id.toByteArray())
        val result = bytes.fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }
        return result.toInt()
    }

    private suspend fun insertPuzzle(row: PuzzleCsvRow): Int = dbQuery {
        Puzzles.insert {
            it[fen] = row.fen
            it[ranking] = row.rating
            it[moves] = row.moves
            it[database] = PuzzleDatabase.LICHESS
            it[id] = getPuzzleId(row)
        } get Puzzles.id
    }

    private suspend fun processRow(row: PuzzleCsvRow) {
        val puzzleId = insertPuzzle(row)
        val themeList = row.themes.split(" ")
        ensureThemes(themeList)
        puzzleThemeService.tryAssignThemes(puzzleId, themeList)
    }

    suspend fun importData() {
        val reader = FileReader(File("lichess.data.csv"))
        PuzzleCsvReader.readRows(reader, ::processRow)
    }
}