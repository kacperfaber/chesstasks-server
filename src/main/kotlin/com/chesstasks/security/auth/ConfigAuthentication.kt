package com.chesstasks.security.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.get


fun Application.configureAuthentication() {
    val tokenAuthenticationProvider: TokenAuthenticationProvider = get()

    install(Authentication) {
        this.register(tokenAuthenticationProvider)
    }
}