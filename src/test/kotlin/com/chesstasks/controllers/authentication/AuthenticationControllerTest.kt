package com.chesstasks.controllers.authentication

import com.chesstasks.data.DatabaseFactory
import com.chesstasks.data.dto.Users
import com.google.gson.Gson
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import testutils.integrationTest
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AuthenticationControllerTest {
    fun HttpRequestBuilder.setAuthBody(login: String, password: String) {
        setBody(Gson().toJson(mapOf("login" to login, "password" to password)))
        contentType(ContentType.Application.Json)
    }

    @Test
    fun `authEndpoint`() {
        integrationTest {
            val response = client.get("/auth/current").status
            println("Debug")
        }
    }
}