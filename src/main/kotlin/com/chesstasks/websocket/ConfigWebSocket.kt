package com.chesstasks.websocket


import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.security.auth.webSocketUser
import com.chesstasks.websocket.handlers.configHandlers
import com.chesstasks.websocket.handlers.training.trainingHandler
import com.chesstasks.websocket.handlers.user.userHandler
import com.chesstasks.websocket.worker.setupEndpointWorker
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
        // TODO: this.contentConverter - Configure contentConverter
    }

    configHandlers {
        userHandler()
        trainingHandler()
    }

    routing {
        webSocketUser {
            webSocket("/play") {
                call.principal<TokenPrincipal>() ?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "forbid"))
                setupEndpointWorker()
            }
        }
    }
}