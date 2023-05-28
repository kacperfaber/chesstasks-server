package com.chesstasks.security

import com.chesstasks.Profile
import com.chesstasks.Profiles
import com.chesstasks.di.setupModules
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.assertDoesNotThrow
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class DevPasswordHasherTest {
    private val passwordHasher: PasswordHasher
        get() = get().get()


    @Test
    fun `comparePasswords does not throw`() {
        runBlocking {
            assertDoesNotThrow { passwordHasher.comparePasswords("", "") }
        }
    }

    @Test
    fun `comparePasswords returns true if value equals`() {
        runBlocking {
            assertTrue { passwordHasher.comparePasswords("abc", "abc") }
        }
    }

    @Test
    fun `comparePasswords returns true if value and reference equals`() {
        val abc = "abc"
        runBlocking {
            assertTrue { passwordHasher.comparePasswords(abc, abc) }
        }
    }

    @Test
    fun `comparePasswords returns true if value and reference doesn't equal`() {
        runBlocking {
            assertTrue { passwordHasher.comparePasswords("abc", "abc") }
        }
    }

    @Test
    fun `comparePasswords returns false if value doesn't equal`() {
        runBlocking {
            assertFalse{passwordHasher.comparePasswords("abc", "def")}
            assertFalse{passwordHasher.comparePasswords("Kacper", "Faber")}
            assertFalse{passwordHasher.comparePasswords("Test", "TEST")}
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun before() {
            Profiles.profileFallback = Profile.DEV

            startKoin {
                setupModules()
            }
        }
    }
}