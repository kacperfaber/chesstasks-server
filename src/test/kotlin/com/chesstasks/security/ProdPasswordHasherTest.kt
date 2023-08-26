package com.chesstasks.security

import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ProdPasswordHasherTest : BaseTest(){
    @Test
    fun `hash does not throw`() = testSuspend {
        assertDoesNotThrow { ProdPasswordHasher().hash("test") }
    }

    @Test
    fun `hash returns another string`() = testSuspend {
        val pass = "test12345!@#$%%"
        assertNotEquals(pass, ProdPasswordHasher().hash(pass))
    }

    @Test
    fun `hash returns same hash from two ProdPasswordHasher instances`() = testSuspend {
        val pass = "test12345!@#$%%"
        assertEquals(ProdPasswordHasher().hash(pass), ProdPasswordHasher().hash(pass))
    }

    @Test
    fun `hash returns hash that can be read using comparePasswords`() = testSuspend {
        val pass = "testTest21424)(_!#$%"
        val hash = ProdPasswordHasher().hash(pass)
        assertTrue { ProdPasswordHasher().comparePasswords(pass, hash) }
    }

    @Test
    fun `comparePasswords does not throw`() = testSuspend {
        assertDoesNotThrow { ProdPasswordHasher().comparePasswords("", "") }
    }

    @Test
    fun `comparePasswords returns true if password matches`() = testSuspend {
        val pass = "Test12345"
        val hasher = ProdPasswordHasher()
        assertTrue { hasher.comparePasswords(pass, hasher.hash(pass)) }
    }

    @Test
    fun `comparePasswords returns false if passwords are bad`() = testSuspend {
        val pass1 = "Test12345"
        val pass2 = "Test123456"
        val hasher = ProdPasswordHasher()
        assertFalse { hasher.comparePasswords(pass1, hasher.hash(pass2)) }
    }
}