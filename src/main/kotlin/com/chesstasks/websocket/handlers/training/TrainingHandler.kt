package com.chesstasks.websocket.handlers.training

import com.chesstasks.game.puzzle.MoveSolveState
import com.chesstasks.game.training.TrainingController
import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.websocket.handlers.Handlers
import com.chesstasks.websocket.handlers.user
import com.chesstasks.websocket.send
import com.chesstasks.websocket.sendCommand
import io.ktor.server.auth.*
import org.koin.java.KoinJavaComponent.inject

class TrainingPayload(private val rankingOffset: Int = 0) {
    fun toCreateSessionSettings(): TrainingController.CreateSessionSettings {
        return TrainingController.CreateSessionSettings(rankingOffset)
    }
}

class TrainingResponse(val ready: Boolean = true)

fun Handlers.Config.trainingHandler() {
    val trainingController by inject<TrainingController>(TrainingController::class.java)

    user {
        handle("_game.training.start") {
            val payload = it.receive<TrainingPayload>()
            val user = call.principal<TokenPrincipal>()?.user ?: throw Exception("Current user must not be null.")
            trainingController.createSession(user, this, payload.toCreateSessionSettings())
            send("game.training.start", TrainingResponse())
        }

        handle("_game.training.move") {
            val move: String = it.receive()
            val moveResult = trainingController.makeMove(this, move)
            sendCommand("game.training.move", moveResult)
        }
    }
}