package com.chesstasks.controllers.puzzle.history

import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
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

        get("/puzzle/history/by-user/{userId}") {
            val authenticatedUserId = call.requirePrincipalId()
            val userId = call.parameters["userId"]?.toIntOrNull() ?: throw MissingQueryParameter("userId")
            val skip = call.parameters["skip"]?.toLongOrNull() ?: 0
            val result = puzzleHistoryService.getByUserId(userId, authenticatedUserId, skip)
            call.ofNullable(result)
        }
    }

    admin {
        get("/puzzle/history/by-id/as-admin/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(puzzleHistoryService.getById(id))
        }
    }
}