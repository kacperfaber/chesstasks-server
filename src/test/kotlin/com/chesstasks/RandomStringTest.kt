package com.chesstasks

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RandomStringTest {
    @Test
    fun `randomString returns expected size`() {
        assertEquals(15, randomString(15).count())
    }

    @Test
    fun `randomString returns another strings each time`() {
        val x = randomString(5)
        val y = randomString(5)
        assertNotEquals(x, y)
    }

    @Test
    fun `randomString returns not the same letter 'x' times`() {
        val r = randomString(10)
        val r1 = r[0]
        val matches = "^$r1{10}\$".toRegex().matches(r)
        assertFalse { matches }
    }

    @Test
    fun `randomString returns string matches to regex`() {
        val r = randomString(10)
        val matches = "^([A-Za-z0-9]){10}\$".toRegex().matches(r)
        assertTrue { matches }
    }
}