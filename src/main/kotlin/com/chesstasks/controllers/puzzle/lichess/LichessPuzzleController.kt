package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.lichess.LichessPuzzleService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Route.lichessPuzzleController() {
    val lichessPuzzleService by inject<LichessPuzzleService>()

    user {
        get("/lichess-puzzle/{id}") {
            val id = call.parameters["id"] ?: throw MissingQueryParameter("id")
            call.ofNullable(lichessPuzzleService.getById(id))
        }
    }

    admin {
        put("/lichess-puzzle") {
            val payload = call.receive<InsertLichessPuzzlePayload>() // TODO: Missing model validation.
            call.ofNullable(lichessPuzzleService.createNew(payload.id, payload.fen, payload.moves, payload.ranking))
        }

        delete("/lichess-puzzle/{id}") {
            val id = call.parameters["id"] ?: throw MissingQueryParameter("id")
            call.ofBoolean(lichessPuzzleService.deleteById(id))
        }
    }
}