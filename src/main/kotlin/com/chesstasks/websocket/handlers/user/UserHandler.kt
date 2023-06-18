package com.chesstasks.websocket.handlers.user

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.websocket.handlers.Handlers
import com.chesstasks.websocket.handlers.user
import com.chesstasks.websocket.send
import io.ktor.server.auth.*

// It's some kinda of example, because this data I will left only in REST API.

fun Handlers.Config.userHandler() {
    user {
        handle("_user.current") {
            val principal = call.principal<TokenPrincipal>()!!.user
            send("user.current", principal)
        }
    }
}