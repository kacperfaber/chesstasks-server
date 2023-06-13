package com.chesstasks.controllers.puzzle

import com.chesstasks.controllers.ofNullable
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.PuzzleService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.get

fun Route.puzzleController() {
    val puzzleService by inject<PuzzleService>(PuzzleService::class.java)

    user {
        get("/puzzle/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(puzzleService.getById(id))
        }

        // TODO: Missing fetch lists
    }
}