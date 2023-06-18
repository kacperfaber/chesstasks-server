package com.chesstasks.websocket.handlers

import io.ktor.server.application.*

fun Application.configHandlers(func: Handlers.Config.() -> Unit) {
    func(Handlers.Config)
}