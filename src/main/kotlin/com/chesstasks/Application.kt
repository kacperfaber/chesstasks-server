package com.chesstasks

import com.chesstasks.controllers.configureControllers
import com.chesstasks.data.configureDb
import com.chesstasks.di.configureDi
import com.chesstasks.exceptions.handlers.configureExceptionHandlers
import com.chesstasks.freemarker.configFreeMarker
import com.chesstasks.requestvalidation.configureRequestValidation
import com.chesstasks.security.auth.configureAuthentication
import com.chesstasks.serialization.configureSerialization
import com.chesstasks.websocket.configWebSocket
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    Profiles.profileFallback = Profile.DEV

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDi()
    configureExceptionHandlers()
    configureDb()
    configureSerialization()
    configureAuthentication()
    configureRequestValidation()
    configFreeMarker()
    configureControllers()
    configWebSocket()
}
