package com.chesstasks.services.importpuzzle

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dao.ThemeDao
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleThemes
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.services.puzzle.themes.PuzzleThemeService
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single
import java.io.File
import java.io.FileReader
import java.security.MessageDigest
import kotlin.random.Random

@Single
class ImportPuzzleService(private val themeDao: ThemeDao, private val puzzleThemeService: PuzzleThemeService) {
    private suspend fun ensureThemes(themes: List<String>) {
        themes
            .filter { !themeDao.isThemeExist(it) }
            .forEach { themeDao.insertTheme(it) }
    }

    private suspend fun ensureTheme(theme: String): Int {
        return themeDao.getThemeId(theme) ?: themeDao.insertTheme(theme)
    }

    private suspend fun createThemeBuffer(fileReader: FileReader): Map<String, Int> {
        val output = mutableMapOf<String, Int>()
        PuzzleCsvReader.readRows(fileReader) { row ->
            val themeList = row.themes.split(" ")
            themeList.forEach { theme ->
                if (!output.containsKey(theme)) {
                    val themeId = ensureTheme(theme)
                    output[theme] = themeId
                }
            }
        }
        return output
    }

    private fun getPuzzleId(row: PuzzleCsvRow): Int {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(row.id.toByteArray())
        val result = bytes.fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }
        return result.toInt() + Random.nextInt()
    }

    private fun insertPuzzle(row: PuzzleCsvRow): Int? {
        val id = getPuzzleId(row)

        try {
            return Puzzles.insert {
                it[fen] = row.fen
                it[ranking] = row.rating
                it[moves] = row.moves
                it[database] = PuzzleDatabase.LICHESS
                it[this.id] = id
            } get Puzzles.id
        }

        catch (e: Exception) {
            val isTaken = isPuzzleIdTaken(id)

            if (isTaken) {
                return tryWithAnotherId(row)
            }

            return null
        }
    }

    private fun tryWithAnotherId(row: PuzzleCsvRow): Int? {
        val id = getPuzzleId(row)

        try {
            return Puzzles.insert {
                it[fen] = row.fen
                it[ranking] = row.rating
                it[moves] = row.moves
                it[database] = PuzzleDatabase.LICHESS
                it[this.id] = id
            } get Puzzles.id
        }

        catch (e: Exception) {
          return null
        }
    }

    private fun isPuzzleIdTaken(id: Int): Boolean {
        return Puzzles.select { Puzzles.id eq id }.count() > 0
    }

    private suspend fun processRow(row: PuzzleCsvRow, themes: Map<String, Int>): Boolean = dbQuery {
        val puzzleId = insertPuzzle(row) ?: return@dbQuery false
        val themeList = row.themes.split(" ")
        themeList.forEach {  theme ->
            val themeId = themes[theme] ?: throw Exception("In buffer there's no $theme mapped.")
            PuzzleThemes.insert {
                it[PuzzleThemes.puzzleId] = puzzleId
                it[PuzzleThemes.themeId] = themeId
            }
        }

        return@dbQuery true
    }

    private fun logResult(row: PuzzleCsvRow, result: Boolean) {
        println("IMPORT-LOG: SUCCESS: true: ${row.id}")
    }

    suspend fun importData() {
        val reader = FileReader(File("lichess.data.csv"))

        val themeBuffer = createThemeBuffer(reader)

        val reader2 = FileReader(File("lichess.data.csv"))

        PuzzleCsvReader.readRows(reader2) {
            val r = processRow(it, themeBuffer)
            logResult(it, r)
        }
    }
}
