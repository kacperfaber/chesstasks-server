package com.chesstasks.controllers.ui

import com.chesstasks.controllers.ui.login.loginController
import com.chesstasks.controllers.ui.register.registerController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureUIControllers() {
    routing {
        configUI()

        route("ui") {
            loginController()
            registerController()
        }
    }
}