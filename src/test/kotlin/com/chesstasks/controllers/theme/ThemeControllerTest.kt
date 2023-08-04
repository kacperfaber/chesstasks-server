package com.chesstasks.controllers.theme

import com.chesstasks.data.dto.Themes
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebTest
import testutils.isForbid
import testutils.isOk
import testutils.jsonPath

class ThemeControllerTest : BaseWebTest() {
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

    private fun setupThemes() {
        transaction {
            Themes.insert {
                it[name] = "English-Opening"
                it[id] = 0
            }

            Themes.insert {
                it[name] = "Caro-Kann-Defense"
                it[id] = 1
            }
        }
    }

    @Test
    fun `allThemesEndpoint returns FORBIDDEN if not authenticated`() = testSuspend {
        setupThemes()
        app.client.get("/api/theme/all").status.isForbid()
    }

    @Test
    fun `allThemeEndpoint returns OK if authenticated`() = testSuspend {
        setupUser()
        setupThemes()
        app.client.get("/api/theme/all") {withToken(0)}.status.isOk()
    }

    @Test
    fun `allThemeEndpoint returns expected items length if authenticated`() = testSuspend {
        setupUser()
        setupThemes()
        app.client.get("/api/theme/all") {withToken(0)}.jsonPath("$.length()", 2)
    }

    @Test
    fun `allThemeEndpoint returns expected items data if authenticated`() = testSuspend {
        setupUser()
        setupThemes()
        val r = app.client.get("/api/theme/all") { withToken(0) }
        r.jsonPath("$[0].id", 0)
        r.jsonPath("$[0].name", "English-Opening")
        r.jsonPath("$[1].id", 1)
        r.jsonPath("$[1].name", "Caro-Kann-Defense")
    }
}