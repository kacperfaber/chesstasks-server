package com.chesstasks.controllers.api

import com.chesstasks.controllers.api.authentication.authenticationController
import com.chesstasks.controllers.api.friend.friendController
import com.chesstasks.controllers.api.play.training.playTrainingController
import com.chesstasks.controllers.api.puzzle.puzzleController
import com.chesstasks.controllers.api.puzzlehistory.puzzleHistoryController
import com.chesstasks.controllers.api.theme.themeController
import com.chesstasks.controllers.api.user.userController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureApiControllers() {
    routing {
        route("/api") {
            userController()
            authenticationController()
            puzzleController()
            themeController()
            friendController()
            playTrainingController()
            puzzleHistoryController()
        }
    }
}