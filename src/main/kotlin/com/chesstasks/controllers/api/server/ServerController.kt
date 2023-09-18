package com.chesstasks.controllers.api.server

import com.chesstasks.services.server.ServerService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.serverController() {
    val serverService by inject<ServerService>()

    get("/server/version/raw") {
        call.respond(serverService.getServerVersion())
    }

    get("/server/version") {
        val v = serverService.getServerVersion()
        call.respond(mapOf("version" to v))
    }
}
