package com.chesstasks.controllers.ui

import com.chesstasks.controllers.ui.login.loginController
import com.chesstasks.controllers.ui.register.registerController
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Route.configUI() {
    staticFiles("/public", File("node_modules")) {}

    staticFiles("/public", File("src/main/resources/public")) {}

    loginController()
    registerController()
}