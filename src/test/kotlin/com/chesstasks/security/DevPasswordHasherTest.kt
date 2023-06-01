package com.chesstasks.security

import com.chesstasks.Profile
import com.chesstasks.Profiles
import com.chesstasks.di.setupModules
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.junit.jupiter.api.assertDoesNotThrow
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import testutils.BaseTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DevPasswordHasherTest : BaseTest() {
    @Test
    fun `comparePasswords does not throw`() = testSuspend {
        runBlocking {
            val passwordHasher = get().get<PasswordHasher>()
            assertDoesNotThrow { passwordHasher.comparePasswords("", "") }
        }
    }

    @Test
    fun `comparePasswords returns true if value equals`() = testSuspend {
        val passwordHasher = get().get<PasswordHasher>()
        assertTrue { passwordHasher.comparePasswords("abc", "abc") }
    }

    @Test
    fun `comparePasswords returns true if value and reference equals`() = testSuspend {
        val abc = "abc"
        val passwordHasher = get().get<PasswordHasher>()
        assertTrue { passwordHasher.comparePasswords(abc, abc) }
    }

    @Test
    fun `comparePasswords returns true if value and reference doesn't equal`() = testSuspend {
        val passwordHasher = get().get<PasswordHasher>()
        assertTrue { passwordHasher.comparePasswords("abc", "abc") }
    }

    @Test
    fun `comparePasswords returns false if value doesn't equal`() = testSuspend {
        val passwordHasher = get().get<PasswordHasher>()
        assertFalse{passwordHasher.comparePasswords("abc", "def")}
        assertFalse{passwordHasher.comparePasswords("Kacper", "Faber")}
        assertFalse{passwordHasher.comparePasswords("Test", "TEST")}
    }
}