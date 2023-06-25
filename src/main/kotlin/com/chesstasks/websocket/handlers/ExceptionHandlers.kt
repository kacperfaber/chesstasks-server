package com.chesstasks.websocket.handlers

import com.chesstasks.websocket.exceptions.GameSessionNotFoundException
import com.chesstasks.websocket.sendCommand

fun Handlers.Config.exceptionHandlers() {
    exception<GameSessionNotFoundException> {
        sendCommand("game_session_not_found")
    }
}