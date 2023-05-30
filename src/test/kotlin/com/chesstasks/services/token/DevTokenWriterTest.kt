package com.chesstasks.services.token

import com.google.gson.Gson
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals

class DevTokenWriterTest : BaseTest() {

    @Test
    fun `writeToken does not throw`() = testSuspend {
        val tokenWriter = getInstance<DevTokenWriter>()
        assertDoesNotThrow { tokenWriter.writeToken(Token(0, "ABC")) }
    }

    @Test
    fun `writeToken returns JSON`() {
        val tokenWriter = getInstance<DevTokenWriter>()
        val res = tokenWriter.writeToken(Token(0, "ABC"))
        assertDoesNotThrow { Gson().fromJson(res, Token::class.java) }
    }

    @Test
    fun `writeToken returns the same token after deserializing`() {
        val id = Random.nextInt()
        val secret = UUID.randomUUID().toString()
        val tokenWriter = getInstance<DevTokenWriter>()
        val res = tokenWriter.writeToken(Token(id, secret))
        val anotherToken = Gson().fromJson(res, Token::class.java)
        assertEquals(id, anotherToken.userId)
        assertEquals(secret, anotherToken.secret)
    }
}

