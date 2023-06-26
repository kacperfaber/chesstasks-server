package com.chesstasks.controllers

import com.chesstasks.controllers.authentication.authenticationController
import com.chesstasks.controllers.friend.friendController
import com.chesstasks.controllers.play.training.playTrainingController
import com.chesstasks.controllers.puzzle.puzzleController
import com.chesstasks.controllers.ui.configUI
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureControllers() {
    routing {
        authenticationController()
        puzzleController()
        friendController()
        playTrainingController()

        // TODO: Embrace this 3 lines above in one 'configUI'

        // TODO: Set prefix on api's endpoint to api/

        configUI()
    }
}