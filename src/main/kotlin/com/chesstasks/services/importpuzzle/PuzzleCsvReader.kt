package com.chesstasks.services.importpuzzle

import com.chesstasks.services.importpuzzle.PuzzleCsvRow.Companion.toPuzzleCsvRow
import com.opencsv.CSVReaderBuilder
import java.io.FileReader

data class PuzzleCsvRow(val id: String, val fen: String, val moves: String, val themes: String, val rating: Int) {
    companion object {
        fun Array<String>.toPuzzleCsvRow(): PuzzleCsvRow = PuzzleCsvRow(this[0], this[1], this[2], this[7], this[3].toInt())
    }
}

object PuzzleCsvReader {
    suspend fun readRows(fileReader: FileReader, processRow: suspend (PuzzleCsvRow) -> Unit) {
        val reader = CSVReaderBuilder(fileReader)
            .withSkipLines(1)
            .build()

        var nextRow: Array<String>?

        while (reader.readNext().also { nextRow = it } != null) {
            val row = nextRow?.toPuzzleCsvRow() ?: break
            processRow(row)
        }
    }
}