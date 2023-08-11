package com.chesstasks.controllers.user.preferences

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals

class UserPreferencesControllerTest : BaseWebTest() {
    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[emailAddress] = "kacperf1234@gmail.com"
            it[passwordHash] = "kacperfaber"
        }

        UserPreferences.insert {
            it[id] = 0
            it[userId] = 0
            it[historyVisibility] = UserPuzzleHistoryVisibility.ONLY_FRIENDS
        }
    }

    @Test
    fun `getHistoryVisibilityEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("/api/user/preferences/history-visibility").status.isForbid()
    }

    @Test
    fun `getHistoryVisibilityEndpoint returns OK if authentication`() = testSuspend {
        setupUser()
        app.client.get("/api/user/preferences/history-visibility") { withToken(0) }.status.isOk()
    }

    @Test
    fun `getHistoryVisibilityEndpoint returns OK and expected body`() = testSuspend {
        setupUser()
        transaction {
            UserPreferences.update({ UserPreferences.id eq 0 }) {
                it[historyVisibility] = UserPuzzleHistoryVisibility.EVERYONE
            }
        }
        val r = app.client.get("/api/user/preferences/history-visibility") { withToken(0) }
        r.status.isOk()
        val res = UserPuzzleHistoryVisibility.valueOf(r.jsonPath<String>("$")!!)

        assertEquals(UserPuzzleHistoryVisibility.EVERYONE, res)
    }

    @Test
    fun `setHistoryVisibilityEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.post("/api/user/preferences/history-visibility/EVERYONE").status.isForbid()
    }

    @Test
    fun `setHistoryVisibilityEndpoint returns NO_CONTENT if authentication`() = testSuspend {
        setupUser()
        app.client.post("/api/user/preferences/history-visibility/EVERYONE") { withToken(0) }.status.isNoContent()
    }

    @Test
    fun `setHistoryVisibilityEndpoint makes UserPreferences-historyVisibility changed to expected value and returns OK`() = testSuspend {
        setupUser()
        app.client.post("/api/user/preferences/history-visibility/ME") { withToken(0) }.status.isNoContent()
        val value = transaction { UserPreferences.select { UserPreferences.id eq 0 }.map { it[UserPreferences.historyVisibility] }.firstOrNull() }
        assertEquals(UserPuzzleHistoryVisibility.ME, value)
    }
}