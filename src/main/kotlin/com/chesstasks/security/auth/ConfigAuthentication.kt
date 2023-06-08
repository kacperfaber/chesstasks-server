package com.chesstasks.security.auth

import com.chesstasks.security.auth.session.configSessionCookie
import com.chesstasks.security.auth.session.configSessionSecurity
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.get


fun Application.configureAuthentication() {
    val tokenAuthenticationProvider: TokenAuthenticationProvider = get()
    val adminTokenAuthenticationProvider: AdminTokenAuthenticationProvider = get()

    install(Authentication) {
        this.register(tokenAuthenticationProvider)
        this.register(adminTokenAuthenticationProvider)
        configSessionSecurity()
    }

    install(Sessions) {
        configSessionCookie()
    }
}