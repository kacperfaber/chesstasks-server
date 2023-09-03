package com.chesstasks.services.token

import com.google.gson.Gson
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProdTokenWriterTest {
    private fun exec(t: Token): String {
        return ProdTokenWriter(Gson()).writeToken(t)
    }

    private fun tryRead(t: String): Token? {
        return ProdTokenReader(Gson()).readToken(t)
    }

    @Test
    fun `writeToken does not throw`() {
        assertDoesNotThrow { exec(Token(0, "")) }
    }

    @Test
    fun `writeToken returns at least 10 characters`() {
        assertTrue { exec(Token(0, "")).length >= 10 }
    }

    @Test
    fun `writeToken returns good string, that ProdTokenReader can read`() {
        val userId = Random.nextInt()
        val secret = UUID.randomUUID().toString()
        val tokenHash = exec(Token(userId, secret))
        val r = tryRead(tokenHash)
        assertEquals(userId, r?.userId)
        assertEquals(secret, r?.secret)
    }
}