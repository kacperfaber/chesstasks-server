package com.chesstasks.websocket.worker

import com.chesstasks.websocket.Command
import com.chesstasks.websocket.exceptions.CommandForbiddenException
import com.chesstasks.websocket.handlers.Handlers
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.websocket.*
import io.ktor.websocket.*

private val objectMapper = ObjectMapper() // TODO: Migrate GSON->Jackson

private val commandForbiddenMessage = Frame.Text("{\"n\": \"command_forbidden\"}")

suspend fun DefaultWebSocketServerSession.setupEndpointWorker() {
    for (frame in incoming) {
        val message = String(frame.readBytes())
        val command = objectMapper.readValue(message, Command::class.java)
        val handler = Handlers.handlers.getOrDefault(command.name, null) ?: continue // TODO()

        try {
            handler.validate(this)
            handler.onReceived(this, command)
        }

        catch (e: CommandForbiddenException) {
            send(commandForbiddenMessage)
        }

        catch (e: Exception) {
            Handlers.tryHandleException(this, e)
        }
    }
}