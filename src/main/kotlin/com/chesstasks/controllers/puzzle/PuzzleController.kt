package com.chesstasks.controllers.puzzle

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.PuzzleService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.puzzleController() {
    val puzzleService by inject<PuzzleService>(PuzzleService::class.java)

    user {
        get("/puzzle/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(puzzleService.getById(id))
        }

        get("/puzzle/all/by-database/lichess") {
            val skip = call.parameters["skip"]?.toLongOrNull() ?: 0
            call.ofNullable(puzzleService.getAllByDatabase(PuzzleDatabase.LICHESS, skip))
        }

        get("/puzzle/all/by-database/user") {
            val skip = call.parameters["skip"]?.toLongOrNull() ?: 0
            call.ofNullable(puzzleService.getAllByDatabase(PuzzleDatabase.USER, skip))
        }

        get("/puzzle/by-user/{userId}") {
            val skip = call.parameters["skip"]?.toLongOrNull() ?: 0
            val userId = call.parameters["userId"]?.toIntOrNull() ?: throw MissingQueryParameter("userId")
            call.ofNullable(puzzleService.getAllByOwner(userId, skip))
        }

        delete("/puzzle/{id}") {
            val userId = call.requirePrincipalId()
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")

            call.ofBoolean(puzzleService.deletePuzzle(id, userId))
        }

        get("/puzzle/by-theme/{themeName}") {
            val themeName = call.parameters["themeName"] ?: throw MissingQueryParameter("themeName")
            call.ofNullable(puzzleService.getAllByThemeName(themeName, call.getSkip()))
        }
    }

    admin {
        delete("/puzzle/as-admin/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofBoolean(puzzleService.deletePuzzle(id))
        }
    }
}