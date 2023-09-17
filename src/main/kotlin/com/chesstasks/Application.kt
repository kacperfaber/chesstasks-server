package com.chesstasks

import com.chesstasks.controllers.configureControllers
import com.chesstasks.cors.configureCors
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
import com.chesstasks.Properties;

private val host by Properties.value<String>("$.server.host")
private val port by Properties.value<Int>("$.server.port")


fun main() {
    Profiles.profileFallback = Profile.DEV

    embeddedServer(Netty, port = port, host = host, module = Application::module)
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
    configureCors()
    configureControllers()
    configWebSocket()
}
