package com.chesstasks.websocket

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.websocket.handlers.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configWebSocket() {

    // Default config.
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    configHandlers {
        handle("test") {
            send("Test passed <3")
        }

        user {
            handle("curr") {
                send("current_user", call.principal<TokenPrincipal>()?.user?.id ?: "null")
            }
        }
    }

    routing {
        webSocket("/test") {
            for (frame in incoming) {
                val msg = String(frame.readBytes())
                val msgSplit = msg.split(" ")
                val handler: Handler = Handlers.handlers.filter { it.key == msgSplit[0] }.values.first()
                handler.validate(this)
                handler.onReceived(this, Command(msgSplit[0], msgSplit.getOrElse(1) {"null"}))
            }
        }
    }
}