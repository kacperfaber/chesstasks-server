package com.chesstasks.game.training

import com.chesstasks.data.dto.UserDto
import com.chesstasks.game.GameSession
import com.chesstasks.game.GameSessions
import com.chesstasks.game.modes.TrainingGameSession
import com.chesstasks.game.modes.startTrainingSession
import io.ktor.server.websocket.*
import org.koin.core.annotation.Single

@Single
class TrainingController {
    class CreateSessionSettings(val rankingOffset: Int) {
        fun toTrainingSessionSettings(): TrainingGameSession.Settings {
            return TrainingGameSession.Settings(rankingOffset)
        }
    }

    fun createSession(user: UserDto, session: DefaultWebSocketServerSession, settings: CreateSessionSettings): GameSession {
        return GameSessions.startTrainingSession(user, settings.toTrainingSessionSettings(), session)
    }
}