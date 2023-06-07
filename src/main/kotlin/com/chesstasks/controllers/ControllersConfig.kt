package com.chesstasks.controllers

import com.chesstasks.controllers.authentication.authenticationController
import com.chesstasks.controllers.puzzle.lichess.lichessPuzzleController
import com.chesstasks.controllers.puzzle.user.userPuzzleController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureControllers() {
    routing {
        authenticationController()
        lichessPuzzleController()
        userPuzzleController()
    }
}