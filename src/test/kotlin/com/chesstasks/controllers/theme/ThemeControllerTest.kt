package com.chesstasks.controllers.theme

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.Themes
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    private fun setupAdmin() = transaction {
        Admins.insert {
            it[userId] = 0
            it[id] = 0
        }
    }

    private fun insertTheme(name: String = "mate") = transaction {
        Themes.insert {
            it[id] = 0
            it[Themes.name] = name
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

    @Test
    fun `putThemeEndpoint returns FORBID if no admin, but just user`() = testSuspend {
        setupUser()
        app.client.put("/api/theme/as-admin/{name}"){withToken(0)}.status.isForbid()
    }

    @Test
    fun `putThemeEndpoint returns OK if admin`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.put("/api/theme/as-admin/mate"){withToken(0)}.status.isOk()
    }

    @Test
    fun `putThemeEndpoint returns BAD_REQUEST if admin, but puzzle already exists`() = testSuspend {
        setupUser()
        setupAdmin()
        insertTheme("mate")
        app.client.put("/api/theme/as-admin/mate"){withToken(0)}.status.isBadRequest()
    }

    private fun isThemeByNameExists(name: String): Boolean = transaction {
        Themes.select { Themes.name eq name }.count() > 0
    }

    @Test
    fun `putThemeEndpoint returns OK and makes Theme record in database`() = testSuspend {
        setupUser()
        setupAdmin()
        assertFalse { isThemeByNameExists("mate") }
        app.client.put("/api/theme/as-admin/mate"){withToken(0)}.status.isOk()
        assertTrue (isThemeByNameExists("mate"))
    }
}