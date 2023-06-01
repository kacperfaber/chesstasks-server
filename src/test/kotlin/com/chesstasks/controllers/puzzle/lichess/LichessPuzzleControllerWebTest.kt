package com.chesstasks.controllers.puzzle.lichess

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.LichessPuzzleDto
import com.chesstasks.data.dto.LichessPuzzles
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
}