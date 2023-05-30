package com.chesstasks.controllers.authentication

import com.chesstasks.data.dto.TokenDto
import com.chesstasks.data.dto.Tokens
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import kotlin.test.assertEquals

class AuthenticationControllerTest : BaseWebTest() {
    fun HttpRequestBuilder.setAuthBody(login: String, password: String) {
        jsonBody("login" to login, "password" to password)
    }

    private fun defaultSetupDbForAuth() {
        transaction {
            Users.insert {
                it[id] = 0
                it[passwordHash] = "HelloWorld123"
                it[username] = "kacper"
                it[emailAddress] = "kacperf1234@gmail.com"
            }
        }
    }

    @Test
    fun `authEndpoint returns OK if valid credentials`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns OK if valid username and password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns OK if valid email and password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if valid username and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if valid email and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("kacperf1234@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if invalid email and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("badEmail@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if invalid username and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("badUsername@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns UNSUPPORTED_MEDIA_TYPE if invalid contentType set to XML`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/auth") {
            setAuthBody("badUsername@gmail.com", "BadPassword")
            contentType(ContentType.Application.Xml)
        }

        assertEquals(HttpStatusCode.UnsupportedMediaType, r.status)
    }

    private fun countTokens(): Long {
        var x: Long? = null
        transaction {
            x = Tokens.selectAll().count()
        }
        return x!!
    }

    @Test
    fun `authEndpoint insert one Token and returns OK`() = testSuspend {
        defaultSetupDbForAuth()

        val before = countTokens()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        val after = countTokens()

        assertEquals(HttpStatusCode.OK, r.status)
        assertEquals(before + 1, after)
    }

    private fun getTokensByUserId(userId: Int): List<TokenDto> {
        return transaction {
            Tokens.select { Tokens.userId eq userId }.map {
                TokenDto(it[Tokens.id], it[Tokens.createdAt], it[Tokens.secret], it[Tokens.userId])
            }
        }
    }

    @Test
    fun `authEndpoint insert Token with expected Token-userId and returns OK`() = testSuspend {
        defaultSetupDbForAuth()

        val before = getTokensByUserId(0).count()

        val r = app.client.post("/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        val after = getTokensByUserId(0).count()

        assertEquals(HttpStatusCode.OK, r.status)
        assertEquals(before+1,after)
    }
}