package com.chesstasks.controllers.puzzle.history

import com.chesstasks.controllers.ofNullable
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.history.PuzzleHistoryService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun Route.puzzleHistoryController() {
    val puzzleHistoryService by inject<PuzzleHistoryService>()

    user {
        get("/puzzle/history/{id}") {
            val authenticatedUserId = call.principal<TokenPrincipal>()!!.user.id
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(puzzleHistoryService.getByIdAndAuthenticatedUser(id, authenticatedUserId))
        }
    }

    admin {
        get("/puzzle/history/by-id/as-admin/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(puzzleHistoryService.getById(id))
        }
    }
}