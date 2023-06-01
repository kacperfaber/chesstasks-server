package com.chesstasks.controllers

import com.chesstasks.controllers.authentication.authenticationController
import com.chesstasks.controllers.puzzle.lichess.lichessPuzzleController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureControllers() {
    routing {
        authenticationController()
        lichessPuzzleController()
    }
}