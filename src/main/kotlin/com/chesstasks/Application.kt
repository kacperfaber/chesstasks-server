package com.chesstasks

import com.chesstasks.controllers.configureControllers
import com.chesstasks.data.configureDb
import com.chesstasks.di.configureDi
import com.chesstasks.serialization.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDi()
    configureDb()
    configureSerialization()
    configureControllers()
}
