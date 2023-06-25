package com.chesstasks.websocket

import com.google.gson.Gson
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.java.KoinJavaComponent.inject

private val gson by inject<Gson> (Gson::class.java) // TODO: Should it be like that?

suspend fun DefaultWebSocketServerSession.send(name: String, data: Any?) {
    val r = gson.toJson(mapOf("n" to name, "d" to data))
    send(Frame.Text(r))
}

suspend fun DefaultWebSocketServerSession.sendCommand(name: String) {
    val r = gson.toJson(mapOf("n" to name))
    send(Frame.Text(r))
}

suspend fun DefaultWebSocketServerSession.sendCommand(name: String, data: Any) {
    val r = gson.toJson(mapOf("n" to name, "d" to data))
    send(Frame.Text(r))
}