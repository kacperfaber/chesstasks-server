package com.chesstasks

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateEmailAddressTest {
    @Test
    fun `validateEmailAddress returns true if expected`() {
        val data = "kacperf1234@gmail.com jkowalr@o2.pl test@com.pl".split(" ")

        for (i in data) {
            assertTrue { i.validateEmailAddress() }
        }
    }

    @Test
    fun `validateEmailAddress returns false if expected`() {
        val data = "jebacdisa@ jebacdisa@c @gmail.com xd.com.pl".split(" ")

        for (i in data) {
            assertFalse { i.validateEmailAddress() }
        }
    }
}