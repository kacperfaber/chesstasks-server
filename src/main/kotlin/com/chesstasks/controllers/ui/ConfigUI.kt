package com.chesstasks.controllers.ui

import com.chesstasks.controllers.ui.login.loginController
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Route.configUI() {
    staticFiles("/public", File("src/main/resources/public")) {
        default("/css/style.css")
    }

    loginController()
}