package com.chesstasks.controllers

import com.chesstasks.controllers.authentication.authenticationController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureControllers() {
    routing {
        authenticationController()
    }
}