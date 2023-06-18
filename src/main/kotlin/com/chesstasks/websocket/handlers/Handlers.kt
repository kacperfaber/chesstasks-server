package com.chesstasks.websocket.handlers

import com.chesstasks.websocket.Command
import io.ktor.server.websocket.*

abstract class Handler {
    abstract suspend fun onReceived(session: DefaultWebSocketServerSession, command: Command)
}

class LambdaHandler(val func: suspend DefaultWebSocketServerSession.(Command) -> Unit) : Handler() {
    override suspend fun onReceived(session: DefaultWebSocketServerSession, command: Command) {
        func(session, command)
    }
}

object Handlers {
    val handlers = mutableMapOf<String, Handler>()

    object Config {
        fun handle(s: String, func: suspend DefaultWebSocketServerSession.(Command) -> Unit) {
            handlers[s] = LambdaHandler(func)
        }

        fun registerHandler(s: String, handler: Handler) {
            handlers[s] = handler
        }
    }
}