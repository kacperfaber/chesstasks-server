package com.chesstasks.controllers.puzzlehistory

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleHistoryItems
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import testutils.isForbid
import testutils.isOk
import testutils.jsonPath

class PuzzleHistoryControllerTest : BaseWebTest() {
    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[emailAddress] = "kacperf1234@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        UserPreferences.insert {
            it[id] = 0
            it[userId] = 0
            it[statisticsVisibility] = UserStatisticsVisibility.EVERYONE
            it[historyVisibility] = UserPuzzleHistoryVisibility.EVERYONE
        }
    }

    private fun setupRandomHistoryItems( to: Int) = transaction {
        repeat(to) { iter ->
            Puzzles.insert {
                it[id] = iter
                it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                it[moves] = "d3d4"
                it[ranking] = 1400
                it[database] = PuzzleDatabase.LICHESS
            }

            PuzzleHistoryItems.insert {
                it[id] = iter
                it[puzzleId] = iter
                it[success] = true
                it[moves] = "e2e4"
                it[userId] = 0
            }
        }
    }

    private fun setupHistoryItems() = transaction {
        Puzzles.insert {
            it[id] = 0
            it[fen] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            it[moves] = "d3d4"
            it[ranking] = 1400
            it[database] = PuzzleDatabase.LICHESS
        }

        PuzzleHistoryItems.insert {
            it[id] = 0
            it[puzzleId] = 0
            it[userId] = 0
            it[moves] = "e2e4"
            it[success] = false
        }
    }

    @Test
    fun `minePuzzleHistoryEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        app.client.get("/api/puzzle-history/mine").status.isForbid()
    }

    @Test
    fun `minePuzzleHistoryEndpoint returns OK if authenticated as user`() = testSuspend {
        setupUser()
        app.client.get("/api/puzzle-history/mine") { withToken(0) }.status.isOk()
    }

    @Test
    fun `minePuzzleHistoryEndpoint returns expected items length and returns OK`() = testSuspend {
        setupUser()
        setupHistoryItems()
        val resp = app.client.get("/api/puzzle-history/mine") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$.length()", 1)
    }

    @Test
    fun `minePuzzleHistoryEndpoint returns expected item value and returns OK`() = testSuspend {
        setupUser()
        setupHistoryItems()
        val resp = app.client.get("/api/puzzle-history/mine") { withToken(0) }
        resp.status.isOk()
        resp.jsonPath("$[0].userId", 0)
        resp.jsonPath("$[0].puzzleId", 0)
        resp.jsonPath("$[0].id", 0)
    }

    @Test
    fun `minePuzzleHistoryEndpoint returns expected items length maximum 50`() = testSuspend {
        setupUser()
        setupRandomHistoryItems(1000)
        val resp = app.client.get("/api/puzzle-history/mine") {withToken(0)}
        resp.jsonPath("$.length()", 50)
    }
}