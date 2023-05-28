package com.chesstasks.exceptions.handlers

import com.chesstasks.security.auth.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandlers() {
    install(StatusPages) {
        exception<UnauthorizedException> {
            call, exception -> call.respond(HttpStatusCode.Unauthorized, exception)
        }
    }
}