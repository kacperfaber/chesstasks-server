package com.chesstasks.controllers.puzzle.user

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.UserPuzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*

class UserPuzzleControllerWebTest : BaseWebTest() {
    private fun setupUser() {
        transaction {
            Users.insert {
                it[Users.id] = 0
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
                it[username] = "kacperfaber"
            }
        }
    }

    private fun setupAdmin() {
        transaction {
            Admins.insert {
                it[Admins.id] = 0
                it[userId] = 0
            }
        }
    }

    private fun setupUserPuzzle() {
        transaction {
            UserPuzzles.insert {
                it[id] = 0
                it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[ownerId] = 0
            }
        }
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication and puzzle doesnt exist`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.get("/user-puzzle/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication but puzzle exist`() = testSuspend {
        setupUser()
        setupAdmin()
        setupUserPuzzle()
        app.client.get("/user-puzzle/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns BAD REQUEST if puzzle doesnt exist and authenticated`() = testSuspend {
        setupUser()
        app.client.get("/user-puzzle/0") {withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns OK and expected data when authenticated`() = testSuspend {
        setupUser()
        setupUserPuzzle()
        val response = app.client.get("/user-puzzle/0") {withToken(0)}

        response.status.isOk()

        response.jsonPath("$.ownerId", 0)
        response.jsonPath("$.id", 0)
        response.jsonPath("$.fen", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        response.jsonPath("$.moves", "e2e4")
        response.jsonPath("$.ranking", 1500)
    }
}