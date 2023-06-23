package com.chesstasks.game

import com.chesstasks.data.dto.UserDto
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet

open class GameSession(val user: UserDto, val session: DefaultWebSocketSession)

object GameSessions {
    val sessions: MutableCollection<GameSession> = Collections.synchronizedCollection(LinkedHashSet<GameSession>())

    inline fun <reified T> get(session: DefaultWebSocketSession): T? {
        val gameSession = sessions.firstOrNull { it.session == session } ?: return null
        return if (gameSession is T) gameSession else null
    }

    fun start(gameSession: GameSession) {
        sessions.add(gameSession)
    }
}