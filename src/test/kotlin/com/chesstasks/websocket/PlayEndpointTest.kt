package com.chesstasks.websocket

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.isActive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.BaseWebSocketTest
import kotlin.test.assertFalse


class PlayEndpointTest : BaseWebSocketTest() {

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

    private fun setupAdmin() {
        transaction {
            Admins.insert {
                it[id] = 0
                it[userId] = 0
            }
        }
    }
    
    @Test
    fun `play endpoint CloseCodes-PROTOCOL_EROR when no authentication`() = testSuspend {
        playEndpoint {
            isProtocolError()
            assertFalse { session.isActive }
        }
    }

    @Test
    fun `play endpoint no closing connection when authenticated as user`() = testSuspend {
        setupUser()
        playEndpoint(0) {
            isNotClosed()
        }
    }

    @Test
    fun `play endpoint no closing when authenticated as admin`() = testSuspend {
        setupUser()
        setupAdmin()
        playEndpoint(0) {
            isNotClosed()
        }
    }
}