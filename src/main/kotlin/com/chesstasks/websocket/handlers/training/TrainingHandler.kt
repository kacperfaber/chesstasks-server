package com.chesstasks.websocket.handlers.training

import com.chesstasks.game.GameSessions
import com.chesstasks.game.modes.TrainingGameSession
import com.chesstasks.game.modes.startTrainingSession
import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.websocket.handlers.Handlers
import com.chesstasks.websocket.handlers.user
import com.chesstasks.websocket.send
import io.ktor.server.auth.*

class TrainingPayload(private val rankingOffset: Int = 0) {
    fun toTrainingSettings(): TrainingGameSession.Settings {
        return TrainingGameSession.Settings(rankingOffset)
    }
}

class TrainingResponse(val ready: Boolean = true)

fun Handlers.Config.trainingHandler() {
    user {
        handle("_game.training.start") {
            val settings = it.receive<TrainingPayload>().toTrainingSettings()
            val user = call.principal<TokenPrincipal>()?.user ?: throw Exception("Current user must not be null.")
            GameSessions.startTrainingSession(user, settings, this)
            send("game.training.start", TrainingResponse())
        }
    }
}