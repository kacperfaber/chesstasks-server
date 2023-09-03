package com.chesstasks.services.token

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProdTokenReaderTest {
    private fun exec(t: String): Token? {
        return ProdTokenReader(Gson()).readToken(t)
    }

    private fun write(userId: Int, secret: String): String {
        return ProdTokenWriter(Gson()).writeToken(Token(userId, secret))
    }

    @Test
    fun `readToken does not throw`() {
        assertDoesNotThrow("Should not throw when token is bad") { exec("") }
        assertDoesNotThrow("Should not throw when token is good") { exec(write(0, "secret")) }
    }

    @Test
    fun `readToken returns null if token is bad`() {
        assertNull(exec("JebacDisaKurwe"))
    }

    @Test
    fun `readToken returns not null if token is good`() {
        assertNotNull(exec(write(0, "secret")))
    }

    @Test
    fun `readToken returns expected data if token is good`() {
        val userId = Random.nextInt()
        val secret = UUID.randomUUID().toString()
        val t = exec(write(userId, secret))
        assertEquals(userId, t?.userId)
        assertEquals(secret, t?.secret)
    }
}