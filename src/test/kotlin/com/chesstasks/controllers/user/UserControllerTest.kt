package com.chesstasks.controllers.user

import com.chesstasks.data.dto.SimpleUserDto
import com.chesstasks.data.dto.Users
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
import org.h2.util.json.JSONArray
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import testutils.isForbid
import testutils.isOk
import testutils.jsonPath
import kotlin.test.assertTrue

class UserControllerTest : BaseWebTest() {

    @Test
    fun `searchByUsernameEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("/api/user/search/by-username?query=kacper").status.isForbid()
    }

    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[emailAddress] = "kacperf1234@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }
    }

    private fun setupUsers() = transaction {
        Users.insert {
            it[id] = 1
            it[username] = "glizda88"
            it[emailAddress] = "glizda88@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        Users.insert {
            it[id] = 2
            it[username] = "glizdka"
            it[emailAddress] = "glizdka@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }
    }

    @Test
    fun `searchByUsernameEndpoint returns OK if authenticated`() = testSuspend {
        setupUser()
        app.client.get("/api/user/search/by-username?query=kacper"){withToken(0)}.status.isOk()
    }

    @Test
    fun `searchByUsernameEndpoint returns expected items length and OK - scenario 1`() = testSuspend {
        setupUser()
        setupUsers()
        val resp = app.client.get("/api/user/search/by-username?query=kacper"){withToken(0)}
        resp.jsonPath("$.length()", 1)
    }

    @Test
    fun `searchByUsernameEndpoint returns expected items length and OK - scenario 2`() = testSuspend {
        setupUser()
        setupUsers()
        val resp = app.client.get("/api/user/search/by-username?query=glizd"){withToken(0)}
        resp.jsonPath("$.length()", 2)
    }

    // TODO: Test returned data [list items] are good.
}