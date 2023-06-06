package com.chesstasks.exceptions.handlers

import com.chesstasks.exceptions.BadRequestException
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.ForbidException
import com.chesstasks.security.auth.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandlers() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, exception ->
            call.respond(HttpStatusCode.Unauthorized, ExceptionResponse(exception.message))
        }

        exception<ForbidException> { call, exception ->
            call.respond(HttpStatusCode.Forbidden, ExceptionResponse(exception.message))
        }

        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }

        exception<MissingQueryParameter> {call, exception ->
            call.respond(HttpStatusCode.BadRequest, "Missing '${exception.queryParameterName}' query parameter.")
        }

        exception<RequestValidationException> {call, exception ->
            call.respond(HttpStatusCode.BadRequest, "Request Validation: ${exception.reasons.joinToString(",") { "'$it'" }}")
        }
    }
}