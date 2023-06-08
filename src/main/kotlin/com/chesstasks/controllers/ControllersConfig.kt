package com.chesstasks.controllers

import com.chesstasks.controllers.authentication.authenticationController
import com.chesstasks.controllers.puzzle.lichess.lichessPuzzleController
import com.chesstasks.controllers.puzzle.user.userPuzzleController
import com.chesstasks.controllers.ui.configUI
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureControllers() {
    routing {
        authenticationController()
        lichessPuzzleController()
        userPuzzleController()

        // TODO: Embrace this 3 lines above in one 'configUI'

        // TODO: Set prefix on api's endpoint to api/

        configUI()
    }
}