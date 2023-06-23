package com.chesstasks.websocket


import com.chesstasks.security.auth.user
import com.chesstasks.websocket.handlers.configHandlers
import com.chesstasks.websocket.handlers.user.userHandler
import com.chesstasks.websocket.worker.setupEndpointWorker
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
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
        userHandler()
    }

    routing {
        user {
            webSocket("/play") {
                setupEndpointWorker()
            }
        }
    }
}