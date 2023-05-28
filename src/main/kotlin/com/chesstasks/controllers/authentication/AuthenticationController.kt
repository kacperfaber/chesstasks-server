package com.chesstasks.controllers.authentication

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.security.auth.tokenAuthentication
import com.chesstasks.services.authentication.AuthenticationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authenticationController() {
    val authenticationService by inject<AuthenticationService>()

    data class AuthPayload(val login: String, val password: String)

    post("/auth") {
        val authPayload = call.receive<AuthPayload>()
        val authResult = authenticationService.tryAuthenticate(authPayload.login, authPayload.password) ?: throw BadRequestException("")
        call.respond(authResult.token)
    }

    tokenAuthentication {
        get("/auth/current") {
            val user = call.principal<TokenPrincipal>()!!.user
            call.respond(user)
        }
    }
}