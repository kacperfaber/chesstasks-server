package com.chesstasks.controllers.puzzle.history

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dto.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertNull

class PuzzleHistoryControllerTest : BaseWebTest() {
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

    private fun setupUser2() {
        transaction {
            Users.insert {
                it[id] = 1
                it[username] = "kacperfaber2"
                it[emailAddress] = "kacperf2@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    private fun setupPuzzleHistory() {
        transaction {
            Puzzles.insert {
                it[id] = 0
                it[ownerId] = 0
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
            }

            PuzzleHistoryItems.insert {
                it[id] = 0
                it[userId] = 0
                it[puzzleId] = 0
                it[moves] = "e2e4"
                it[createdAt] = System.currentTimeMillis()
            }
        }
    }

    private fun setupRandomPuzzleHistory(r: Int, from: Int) {
        transaction {
            Puzzles.insert {
                it[id] = 5
                it[ownerId] = 0
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
            }

            repeat(r) { iter ->
                val i = iter + from
                PuzzleHistoryItems.insert {
                    it[id] = i
                    it[userId] = 0
                    it[puzzleId] = 5
                    it[moves] = "e2e4"
                }
            }
        }
    }

    private fun setupHistoryVisibility(historyVisibility: UserPuzzleHistoryVisibility = UserPuzzleHistoryVisibility.EVERYONE) {
        transaction {
            UserPreferences.insert {
                it[id] = 0
                it[UserPreferences.historyVisibility] = historyVisibility
                it[userId] = 0
            }
        }
    }

    @Test
    fun `byIdEndpoint returns FORBIDDEN if no authentication passed`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0").status.isForbid()
    }

    @Test
    fun `byIdEndpoint returns BAD_REQUEST if authenticated as user but resource does not exist`() = testSuspend {
        setupUser()
        app.client.get("/puzzle/history/0") { withToken(0) }.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns BAD_REQUEST if authenticated as user but user_id not match`() = testSuspend {
        setupUser()
        setupUser2()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0") { withToken(1) }.status.isBadRequest()
    }

    @Test
    fun `byIdEndpoint returns OK if authenticated as user and resource exists`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        app.client.get("/puzzle/history/0") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byIdEndpoint returns OK and expected ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.id", 0)
    }

    @Test
    fun `byIdEndpoint returns OK and expected USER_ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.userId", 0)
    }

    @Test
    fun `byIdEndpoint returns OK and expected PUZZLE_ID`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.puzzleId", 0)
    }

    @Test
    fun `byIdEndpoint returns NULL or EMPTY 'user' object when OK`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        assertNull(r.jsonPath("$.user"))
    }

    @Test
    fun `byIdEndpoint returns NULL or EMPTY 'puzzle' object when OK`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        val r = app.client.get("/puzzle/history/0") { withToken(0) }
        r.status.isOk()
        assertNull(r.jsonPath("$.puzzle"))
    }

    @Test
    fun `byUserEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.EVERYONE)
        app.client.get("/puzzle/history/by-user/0").status.isForbid()
    }

    @Test
    fun `byUserEndpoint returns OK authenticated as owner (user_id)`() = testSuspend {
        setupUser()
        setupPuzzleHistory()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.EVERYONE)
        app.client.get("/puzzle/history/by-user/0") { withToken(0) }.status.isOk()
    }

    @Test
    fun `byUserEndpoint returns OK authenticated as other user but target set historyVisibility to EVERYONE`() =
        testSuspend {
            setupUser()
            setupUser2()
            setupPuzzleHistory()
            setupHistoryVisibility(UserPuzzleHistoryVisibility.EVERYONE)
            app.client.get("/puzzle/history/by-user/0") { withToken(1) }.status.isOk()
        }

    @Test
    fun `byUserEndpoint returns BAD_REQUEST authenticated as other user but target set historyVisibility to ME`() =
        testSuspend {
            setupUser()
            setupUser2()
            setupPuzzleHistory()
            setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
            app.client.get("/puzzle/history/by-user/0") { withToken(1) }.status.isBadRequest()
        }

    @Test
    fun `byUserEndpoint returns expected list LENGTH and returns OK`() = testSuspend {
        setupUser()
        setupUser2()
        setupPuzzleHistory()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
        val r = app.client.get("/puzzle/history/by-user/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 1)
    }

    @Test
    fun `byUserEndpoint returns TOTAL list length eq to 50 and returns OK`() = testSuspend {
        setupUser()
        setupUser2()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
        setupRandomPuzzleHistory(500, 0)
        val r = app.client.get("/puzzle/history/by-user/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$.length()", 50)
    }

    private suspend fun idRange(r: HttpResponse, arr: IntRange, skip: Int) {
        arr.forEach { i -> r.jsonPath("$[${i + skip}].id", i) }
    }

    @Test
    fun `byUserEndpoint returns first and last item andd returns OK`() = testSuspend {
        setupUser()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
        setupRandomPuzzleHistory(500, 0)
        val r = app.client.get("/puzzle/history/by-user/0") { withToken(0) }
        r.status.isOk()
        r.jsonPath("$[0].id", 0)
        r.jsonPath("$[49].id", 49)
    }

    @Test
    fun `byUserEndpoint returns all IDs we expect and returns OK`() = testSuspend {
        setupUser()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
        setupRandomPuzzleHistory(500, 0)
        val r = app.client.get("/puzzle/history/by-user/0") { withToken(0) }
        r.status.isOk()
        idRange(r, 0..49, 0)
    }

    @Test
    fun `byUserEndpoint returns all IDs we expect with SKIP and returns OK`() = testSuspend {
        setupUser()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)
        setupRandomPuzzleHistory(500, 0)
        val r = app.client.get("/puzzle/history/by-user/0?skip=100") { withToken(0) }
        r.status.isOk()
        idRange(r, 100..149, -100)
    }



    @Test
    fun `byUserEndpoint returns expected item-data and returns OK`() = testSuspend {
        setupUser()
        setupHistoryVisibility(UserPuzzleHistoryVisibility.ME)

        val created = System.currentTimeMillis()

        transaction {
            Puzzles.insert {
                it[id] = 3
                it[ownerId] = 0
                it[fen] = "8/8/8/8"
                it[moves] = "e2e4"
                it[ranking] = 1500
                it[database] = PuzzleDatabase.USER
            }

            PuzzleHistoryItems.insert {
                it[id] = 5
                it[userId] = 0
                it[puzzleId] = 3
                it[moves] = "e2e4 d2d4"
                it[createdAt] = created
            }
        }

        val r = app.client.get("/puzzle/history/by-user/0") {withToken(0)}
        r.status.isOk()
        r.jsonPath("$[0].moves", "e2e4 d2d4")
        r.jsonPath("$[0].puzzleId", 3)
        r.jsonPath("$[0].id", 5)
        r.jsonPath("$[0].userId", 0)
        r.jsonPath("$[0].createdAt", created)
    }
}