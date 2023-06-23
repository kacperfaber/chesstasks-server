package com.chesstasks.game.modes

import com.chesstasks.data.dto.UserDto
import com.chesstasks.game.GameSession
import io.ktor.server.websocket.*

class TrainingGameSession(user: UserDto, val rankingDifference: Int, val themeId: Int?, session: DefaultWebSocketServerSession) : GameSession(user, session) {
    companion object {
        fun new(user: UserDto, rankingDifference: Int, themeId: Int?, session: DefaultWebSocketServerSession): TrainingGameSession {
            return TrainingGameSession(user, rankingDifference, themeId, session)
        }
    }
}
