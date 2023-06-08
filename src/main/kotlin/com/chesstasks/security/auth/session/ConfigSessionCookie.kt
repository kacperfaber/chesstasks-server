package com.chesstasks.security.auth.session

import io.ktor.server.sessions.*

private const val SESSION_COOKIE_NAME = "user";

fun SessionsConfig.configSessionCookie() {
    cookie<UserSession>(SESSION_COOKIE_NAME) {
        cookie.maxAgeInSeconds = 3600
        cookie.path = "/"
    }
}