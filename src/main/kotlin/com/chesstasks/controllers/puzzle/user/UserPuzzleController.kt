package com.chesstasks.controllers.puzzle.user

import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.user.UserPuzzleService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun Route.userPuzzleController() {
    val userPuzzleService: UserPuzzleService by inject<UserPuzzleService>()

    user {
        get("/user-puzzle/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            val userPuzzle = userPuzzleService.getById(id)
            call.ofNullable(userPuzzle)
        }

        get("/user-puzzle/by-owner/{ownerId}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            val userPuzzles = userPuzzleService.getByOwnerId(id)
            call.ofNullable(userPuzzles)
        }

        put("/user-puzzle/") {
            val payload = call.receive<InsertUserPuzzlePayload>()
            val userId = call.requirePrincipalId()
            call.ofNullable(userPuzzleService.createNew(userId, payload.fen, payload.moves, payload.ranking))
        }

        delete("/user-puzzle/as-owner/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            val userId = call.requirePrincipalId()
            call.ofBoolean(userPuzzleService.deleteAsOwner(userId, id))
        }
    }

    admin {
        delete("/user-puzzle/as-admin/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofBoolean(userPuzzleService.deleteById(id))
        }
    }
}