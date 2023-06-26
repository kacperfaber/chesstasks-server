package com.chesstasks.controllers

import com.chesstasks.controllers.api.configureApiControllers
import com.chesstasks.controllers.ui.configureUIControllers
import io.ktor.server.application.*

fun Application.configureControllers() {
    configureApiControllers()

    configureUIControllers()
}