package com.chesstasks.websocket.worker

import com.chesstasks.websocket.Command
import com.chesstasks.websocket.handlers.Handlers
import com.google.gson.Gson
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.java.KoinJavaComponent.inject

private val gson by inject<Gson>(Gson::class.java)

suspend fun DefaultWebSocketServerSession.setupEndpointWorker() {
    for (frame in incoming) {
        val message = String(frame.readBytes())
        val command = gson.fromJson(message, Command::class.java)
        val handler = Handlers.handlers.getOrDefault(command.name, null) ?: continue // TODO()
        handler.validate(this)
        handler.onReceived(this, command)
    }
}