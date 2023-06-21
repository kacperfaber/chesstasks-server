package com.chesstasks.controllers.ui.login

import com.chesstasks.security.auth.session.UserSession
import com.chesstasks.security.auth.session.userUI
import com.chesstasks.services.authentication.AuthenticationService
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.java.KoinJavaComponent.inject

data class LoginModel(val login: String, val isError: Boolean)

fun Route.loginController() {
    val authenticationService by inject<AuthenticationService>(AuthenticationService::class.java)

    get("/ui/login") {
        val login = call.parameters["login"] ?: ""
        val isError = call.parameters["error"]?.toBoolean() ?: false
        call.respond(FreeMarkerContent("login/login.ftl", LoginModel(login, isError)))
    }

    post("/ui/login/submit") {
        val parameters = call.receiveParameters()
        val login = parameters["login"] ?: throw Exception("missing login form param")
        val password = parameters["password"] ?: throw Exception("missing password form param")
        val authResult = authenticationService.tryAuthenticateSession(login, password)
        if (authResult==null) {
            call.respondRedirect("/ui/login?error=true")
            return@post
        }
        call.sessions.set("user", UserSession(authResult.userId, authResult.emailAddress, authResult.username))
        call.respondRedirect("/ui/home")
    }
}