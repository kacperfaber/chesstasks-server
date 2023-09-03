package com.chesstasks.controllers.api.authentication

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.security.auth.user
import com.chesstasks.services.authentication.AuthenticationService
import com.chesstasks.services.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

data class AuthPayload(val login: String, val password: String)

data class AuthResponse(val token: String)

fun Route.authenticationController() {
    val authenticationService by inject<AuthenticationService>()
    val tokenService by inject<TokenService>()

    post("/auth") {
        val authPayload = call.receive<AuthPayload>()
        val authResult = authenticationService.tryAuthenticate(authPayload.login, authPayload.password) ?: throw BadRequestException("")
        call.respond(AuthResponse(authResult.token))
    }

    user {
        get("/auth/current") {
            val user = call.principal<TokenPrincipal>()!!.user
            call.respond(user)
        }

        post("/auth/revoke") {
            val token = call.principal<TokenPrincipal>()!!.token
            val result = tokenService.revokeToken(token)
            call.respond(if (result) HttpStatusCode.OK else HttpStatusCode.BadRequest)
        }
    }
}