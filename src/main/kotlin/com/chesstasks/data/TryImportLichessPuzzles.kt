package com.chesstasks.data

import com.chesstasks.Constants
import com.chesstasks.services.importpuzzle.ImportPuzzleService
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject

fun Application.tryImportLichessPuzzles() {
    val importPuzzleService by inject<ImportPuzzleService>()

    try {
        if (System.getProperty(Constants.ImportLichessPuzzlesVar, null)?.toBoolean() == true) {
            runBlocking {
                importPuzzleService.importData()
            }
        }
    }

    catch (e: Exception) {
        println("No import set.")
        e.printStackTrace()
    }
}