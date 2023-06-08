package com.chesstasks.security.auth.session

import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.adminUI(act: Route.() -> Unit) {
    authenticate(ADMIN_SESSION_AUTH_NAME) {
        act(this)
    }
}

fun Route.userUI(act: Route.() -> Unit) {
    authenticate(USER_SESSION_AUTH_NAME) {
        act(this)
    }
}