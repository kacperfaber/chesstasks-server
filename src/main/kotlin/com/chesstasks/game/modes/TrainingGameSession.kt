package com.chesstasks.game.modes

import com.chesstasks.data.dto.UserDto
import com.chesstasks.game.GameSession
import com.chesstasks.game.GameSessions
import com.chesstasks.game.puzzle.PuzzleState
import io.ktor.server.websocket.*

class TrainingGameSession(user: UserDto, val settings: Settings, session: DefaultWebSocketServerSession) :
    GameSession(user, session) {
    data class Settings(val rankingOffset: Int = 0)

    var current: PuzzleState? = null
}

fun GameSessions.startTrainingSession(
    user: UserDto,
    settings: TrainingGameSession.Settings,
    session: DefaultWebSocketServerSession): TrainingGameSession
{
    return TrainingGameSession(user, settings, session).apply { start(this) }
}
