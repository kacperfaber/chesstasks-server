package com.chesstasks.controllers.api

import com.chesstasks.controllers.api.authentication.authenticationController
import com.chesstasks.controllers.api.friend.friendController
import com.chesstasks.controllers.api.play.training.playTrainingController
import com.chesstasks.controllers.api.puzzle.puzzleController
import com.chesstasks.controllers.api.theme.themeController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureApiControllers() {
    routing {
        route("/api") {
            authenticationController()
            puzzleController()
            themeController()
            friendController()
            playTrainingController()
        }
    }
}