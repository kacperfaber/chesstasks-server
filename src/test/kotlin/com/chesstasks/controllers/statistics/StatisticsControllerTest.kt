package com.chesstasks.controllers.statistics

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleHistoryItems
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import testutils.*

class StatisticsControllerTest : BaseWebTest() {
    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[emailAddress] = "kacperf1234@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        UserPreferences.insert {
            it[statisticsVisibility] = UserStatisticsVisibility.EVERYONE
            it[id] = 1
            it[userId] = 0
            it[historyVisibility] = UserPuzzleHistoryVisibility.EVERYONE
        }
    }

    private fun setupTestData() = transaction {
        Users.insert {
            it[id] = 1
            it[username] = "test"
            it[emailAddress] = "test@gmail.com"
            it[passwordHash] = "test"
        }

        Puzzles.insert {
            it[id] = 0
            it[ownerId] = 0
            it[fen] = "8/8/8/8"
            it[moves] = "e2e4"
            it[ranking] = 1500
            it[database] = PuzzleDatabase.USER
        }

        UserPreferences.insert {
            it[statisticsVisibility] = UserStatisticsVisibility.EVERYONE
            it[id] = 0
            it[userId] = 1
            it[historyVisibility] = UserPuzzleHistoryVisibility.EVERYONE
        }

        PuzzleHistoryItems.insert {
            it[id] = 1
            it[userId] = 1
            it[moves] = "e2e4"
            it[puzzleId] = 0
            it[success] = false
        }

        PuzzleHistoryItems.insert {
            it[id] = 2
            it[moves] = "e2e4"
            it[userId] = 1
            it[puzzleId] = 0
            it[success] = true
        }

        PuzzleHistoryItems.insert {
            it[id] = 3
            it[userId] = 1
            it[moves] = "e2e4"
            it[puzzleId] = 0
            it[success] = true
        }
    }

    private fun setStatisticsVisibility(value: UserStatisticsVisibility, userId: Int = 1) = transaction {
        UserPreferences.update({UserPreferences.userId eq userId}) { it[statisticsVisibility] = value }
    }

    @Test
    fun `simpleStatisticsEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("api/statistics/simple/user/1").status.isForbid()
    }

    @Test
    fun `simpleStatisticsEndpoint returns BAD_REQUEST if authentication but statisticsVisibility denies`() = testSuspend {
        setupUser()
        setupTestData()
        setStatisticsVisibility(UserStatisticsVisibility.ME)
        app.client.get("api/statistics/simple/user/1"){withToken(0)}.status.isBadRequest()
    }

    @Test
    fun `simpleStatisticsEndpoint returns OK if authentication and statisticsVisibility accepts`() = testSuspend {
        setupUser()
        setupTestData()
        setStatisticsVisibility(UserStatisticsVisibility.EVERYONE)
        app.client.get("api/statistics/simple/user/1"){withToken(0)}.status.isOk()
    }

    @Test
    fun `simpleStatisticsEndpoint returns OK if authentication and target user is authenticated user`() = testSuspend {
        setupUser()
        setupTestData()
        setStatisticsVisibility(UserStatisticsVisibility.EVERYONE)
        app.client.get("api/statistics/simple/user/0"){withToken(0)}.status.isOk()
    }

    @Test
    fun `simpleStatisticsEndpoint returns OK if authentication and target user is authenticated user and expected data`() = testSuspend {
        setupUser()
        setupTestData()
        val r = app.client.get("api/statistics/simple/user/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.totalFails", 0)
        r.jsonPath("$.totalSolved", 0)
    }

    @Test
    fun `simpleStatisticsEndpoint returns OK if authentication and authenticated user is allowed to see statistics - and returns expected data`() = testSuspend {
        setupUser()
        setupTestData()
        setStatisticsVisibility(UserStatisticsVisibility.EVERYONE)
        val r = app.client.get("api/statistics/simple/user/1") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.totalFails", 1)
        r.jsonPath("$.totalSolved", 2)
    }
}