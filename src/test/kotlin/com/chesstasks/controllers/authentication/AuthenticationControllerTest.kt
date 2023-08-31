package com.chesstasks.controllers.authentication

import com.chesstasks.data.dto.*
import com.chesstasks.data.dto.LoginCounters.counter
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

        val r = app.client.post("/api/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns OK if valid username and password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns OK if valid email and password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        assertEquals(HttpStatusCode.OK, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if valid username and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("kacper", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if valid email and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("kacperf1234@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if invalid email and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("badEmail@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST if invalid username and bad password`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
            setAuthBody("badUsername@gmail.com", "BadPassword")
        }

        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `authEndpoint returns UNSUPPORTED_MEDIA_TYPE if invalid contentType set to XML`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.post("/api/auth") {
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

        val r = app.client.post("/api/auth") {
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

        val r = app.client.post("/api/auth") {
            setAuthBody("kacper", "HelloWorld123")
        }

        val after = getTokensByUserId(0).count()

        assertEquals(HttpStatusCode.OK, r.status)
        assertEquals(before+1,after)
    }

    @Test
    fun `currentEndpoint returns 403 if no authentication`() = testSuspend {
        defaultSetupDbForAuth()

        app.client.get("/api/auth/current").status.isForbid()
    }

    @Test
    fun `currentEndpoint returns OK if valid token authentication`() = testSuspend {
        defaultSetupDbForAuth()

        app.client.get("/api/auth/current") {
            withToken(0)
        }.status.isOk()
    }

    @Test
    fun `currentEndpoint returns OK with not null data`() = testSuspend {
        defaultSetupDbForAuth()

        val user = app.client.get("/api/auth/current") {
            withToken(0)
        }.fromJson<UserDto>()

        assertNotNull(user)
    }

    @Test
    fun `currentEndpoint returns OK with expected data`() = testSuspend {
        defaultSetupDbForAuth()

        val user = app.client.get("/api/auth/current") {
            withToken(0)
        }.fromJson<UserDto>()

        assertEquals(0, user.id)
        assertEquals("kacper", user.username)
        assertEquals("kacperf1234@gmail.com", user.emailAddress)
    }

    @Test
    fun `currentEndpoint returns passwordHash null`() = testSuspend {
        defaultSetupDbForAuth()

        val r = app.client.get("/api/auth/current") {
            withToken(0)
        }

        assertNull(r.jsonPath<String>("$.passwordHash"))
    }

    @Test
    fun `authEndpoint returns BAD_REQUEST when good credentials but account is blocked using LoginCounters`() = testSuspend {
        transaction {
            Users.insert {
                it[id] = 0
                it[passwordHash] = "HelloWorld123"
                it[username] = "kacper"
                it[emailAddress] = "kacperf1234@gmail.com"
            }

            LoginCounters.insert {
                it[id] = 0
                it[userId] = 0
                it[counter] = 11
            }
        }

        app.client.post("/api/auth") {setAuthBody("kacper", "HelloWorld123")}.status.isBadRequest()
    }

    @Test
    fun `authEndpoint returns OK when good credentials and resets LoginCounter value to 0`() = testSuspend {
        transaction {
            Users.insert {
                it[id] = 0
                it[passwordHash] = "HelloWorld123"
                it[username] = "kacper"
                it[emailAddress] = "kacperf1234@gmail.com"
            }

            LoginCounters.insert {
                it[id] = 0
                it[userId] = 0
                it[counter] = 5
            }
        }

        app.client.post("/api/auth") {setAuthBody("kacper", "HelloWorld123")}.status.isOk()

        val counter = transaction {
            LoginCounters.select { LoginCounters.userId eq 0 }.map { it[counter] }.singleOrNull()
        }

        assertNotNull(counter)
        assertEquals(0, counter)
    }
}