package com.chesstasks.websocket.handlers.training

import com.chesstasks.data.dto.Users
import com.chesstasks.game.GameSessions
import com.chesstasks.game.modes.TrainingGameSession
import io.ktor.test.dispatcher.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebSocketTest
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrainingHandlerTest : BaseWebSocketTest() {
    private val start = Frame.Text("{\"n\": \"_game.training.start\", \"d\": {\"rankingOffset\": 300}}")

    private fun setupUser() {
        transaction {
            Users.insert {
                it[id] = 0
                it[username] = "kacperfaber"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    @Test
    fun `_game_training_start will respond with game_training_start`() = testSuspend {
        setupUser()

        playEndpoint(0) {
            session.send(start)
            getCommand("game.training.start")
        }
    }

    @Test
    fun `_game_training_start will respond game_training_start with data ready=true`() = testSuspend {
        setupUser()

        playEndpoint(0) {
            session.send(start)
            val ready = getValue<Boolean>("game.training.start", "$.d.ready")
            assertTrue { ready }
        }
    }

    @Test
    fun `_game_training_start makes GameSessions length greater by 1 and returns ready=true`() = testSuspend {
        setupUser()

        playEndpoint(0) {
            val bef = GameSessions.sessions.count()
            session.send(start)
            assertTrue { getValue("game.training.start", "$.d.ready") }
            val now = GameSessions.sessions.count()
            assertEquals(bef + 1, now)
        }
    }

    @Test
    fun `_game_training_start makes GameSession with expected rankingOffset value and returns ready=true`() =
        testSuspend {
            setupUser()

            val expected = Random.nextInt(-200, 200)

            val command = Frame.Text("{\"n\": \"_game.training.start\", \"d\": {\"rankingOffset\": $expected}}")

            fun getGameSession(): TrainingGameSession? {
                return GameSessions.sessions
                    .filterIsInstance<TrainingGameSession>()
                    .firstOrNull {
                        it.settings.rankingOffset == expected
                    }
            }

            playEndpoint(0) {
                session.send(command)

                assertNull(getGameSession())

                assertTrue { getValue("game.training.start", "$.d.ready") }

                assertNotNull(getGameSession())
            }

        }
}
