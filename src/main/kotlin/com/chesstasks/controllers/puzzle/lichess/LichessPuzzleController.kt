package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.controllers.ofNullable
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.tokenAuthentication
import com.chesstasks.services.puzzle.lichess.LichessPuzzleService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.lichessPuzzleController() {
    val lichessPuzzleService by inject<LichessPuzzleService>()

    tokenAuthentication {
        get("/lichess-puzzle/{id}") {
            val id = call.parameters["id"] ?: throw MissingQueryParameter("id")
            call.ofNullable(lichessPuzzleService.getById(id))
        }
    }
}