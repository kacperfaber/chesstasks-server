package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.data.dto.LichessPuzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import testutils.isBadRequest
import testutils.isForbid
import testutils.isOk

class LichessPuzzleControllerWebTest : BaseWebTest() {
    private fun setupLichessPuzzleTable() {
        transaction {
            LichessPuzzles.insert {
                it[id] = "a"
                it[fen] = "8/8/8/8/8/8/8..."
                it[moves] = "e2e4"
                it[opening] = "Queen's Gambit"
                it[ranking] = 1800
            }
        }
    }

    private fun setupUser() {
        transaction {
            Users.insert {
                it[id] = 0
                it[passwordHash] = "HelloWorld123"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[username] = "kacper"
            }
        }
    }

    @Test
    fun `byIdEndpoint returns OK if record exist and authenticated`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.get("/lichess-puzzle/a") {
            withToken(0, "abc")
        }.status.isOk()
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if record exist and not authenticated`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.get("/lichess-puzzle/a").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns BAD REQUEST if record doesn't exist`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.get("/lichess-puzzle/badid") {
          withToken(0)
        }.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns Bif record doesn't exist`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.get("/lichess-puzzle/badid") {
            withToken(0)
        }.status.isBadRequest()
    }
}