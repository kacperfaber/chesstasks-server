package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.LichessPuzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LichessPuzzleControllerWebTest : BaseWebTest() {
    private fun setupLichessPuzzleTable() {
        transaction {
            LichessPuzzles.insert {
                it[id] = "a"
                it[fen] = "8/8/8/8/8/8/8..."
                it[moves] = "e2e4"
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

    private fun setupAdminForUser() {
        transaction {
            Admins.insert {
                it[id] = 0
                it[userId] = 0
                it[createdAt] = System.currentTimeMillis()
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

    @Test
    fun `deleteByIdEndpoint returns NO_CONTENT when record exist and authenticated as admin`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.delete("/lichess-puzzle/a") {
            withToken(0)
        }.status.isNoContent()
    }

    @Test
    fun `deleteByIdEndpoint returns FORBID when record exist and authenticated as user`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.delete("/lichess-puzzle/a") {
            withToken(0)
        }.status.isForbid()
    }

    @Test
    fun `deleteByIdEndpoint returns BAD_REQUEST when record dont exist and authenticated as admin`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.delete("/lichess-puzzle/badid") {
            withToken(0)
        }.status.isBadRequest()
    }

    private fun countRecords(id: String): Long {
        return transaction {
            LichessPuzzles.select { LichessPuzzles.id eq id }.count()
        }
    }

    @Test
    fun `deleteByIdEndpoint deletes record from database when returns NO_CONTENT`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        val before = countRecords("a")

        val status = app.client.delete("/lichess-puzzle/a") {
            withToken(0)
        }.status

        val after = countRecords("a")

        assertEquals(HttpStatusCode.NoContent, status)
        assertEquals(before - 1, after)
    }

    @Test
    fun `deleteByIdEndpoint dont deletes record from database when returns FORBID`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        val before = countRecords("a")

        val status = app.client.delete("/lichess-puzzle/a") {
            withToken(0)
        }.status

        val after = countRecords("a")

        assertEquals(HttpStatusCode.Forbidden, status)
        assertEquals(before, after)
    }

    @Test
    fun `deleteByIdEndpoint dont deletes record from database when returns BAD_REQUEST`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        val before = countRecords("a")

        val status = app.client.delete("/lichess-puzzle/badid") {
            withToken(0)
        }.status

        val after = countRecords("a")

        assertEquals(HttpStatusCode.BadRequest, status)
        assertEquals(before, after)
    }

    @Test
    fun `putEndpoint returns Forbidden if not authenticated`() = testSuspend {
        setupLichessPuzzleTable()

        app.client.put("/lichess-puzzle").status.isForbid()
    }

    @Test
    fun `putEndpoint returns FORBIDDEN if authenticated as a just user`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
        }.status.isForbid()
    }

    private fun HttpRequestBuilder.setInsertPayload(id: String, fen: String, moves: String, ranking: Int) {
        jsonBody("id" to id, "fen" to fen, "moves" to moves, "ranking" to ranking)
    }

    @Test
    fun `putEndpoint returns BAD REQUEST if authenticated but FEN and moves are invalid`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e5e6", 1500)
        }.status.isBadRequest()
    }

    @Test
    fun `putEndpoint returns OK if payload is valid`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", 1500)
        }.status.isOk()
    }

    @Test
    fun `putEndpoint returns BAD_REQUEST if FEN is valid but moves are empty string`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "", 1500)
        }.status.isBadRequest()
    }

    @Test
    fun `putEndpoint returns BAD_REQUEST if ranking is not in range 300-5000`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", 250)
        }.status.isBadRequest()
    }

    @Test
    fun `putEndpoint returns BAD_REQUEST if ID is already in database`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("a", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", 2000)
        }

        transaction {
            val select = LichessPuzzles.select { LichessPuzzles.ranking greater 0 }
            println(select.count())
        }
    }

    @Test
    fun `putEndpoint makes LichessPuzzle table greater by 1 and returns OK`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        val before = transaction { LichessPuzzles.selectAll().count() }

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", 2000)
        }.status.isOk()

        val now = transaction { LichessPuzzles.selectAll().count() }

        assertEquals(before + 1, now)
    }

    @Test
    fun `putEndpoint inserts given data and returns OK`() = testSuspend {
        setupLichessPuzzleTable()
        setupUser()
        setupAdminForUser()

        fun countRecords(): Long {
            return transaction {
                LichessPuzzles.select {
                    (LichessPuzzles.id eq "abc") and (LichessPuzzles.fen eq "rnbqkbnr/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") and (LichessPuzzles.ranking eq 2000) and (LichessPuzzles.moves eq "e2e4")
                }.count()
            }
        }

        val before = countRecords()

        app.client.put("/lichess-puzzle") {
            withToken(0)
            setInsertPayload("abc", "rnbqkbnr/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", 2000)
        }.apply { println(bodyAsText()) }.status.isOk()

        val now = countRecords()

        assertEquals(0L, before)
        assertEquals(1L, now)
    }
}