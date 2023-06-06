package com.chesstasks.services.chess

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testutils.BaseTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChessServiceTest : BaseTest() {
    private val startingPos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    @Test
    fun `validatePose does not throw`() {
        val service = getInstance<ChessService>()
        assertDoesNotThrow {
            service.validatePose(startingPos, "e2e4")
            service.validatePose("", "e2e4")
        }
    }

    @Test
    fun `validatePose returns false when expected`() {
        val service = getInstance<ChessService>()
        assertFalse {
            service.validatePose("", "e2e4")
        }
        assertFalse{
            service.validatePose(startingPos, "e7e5")
        }
        assertFalse{
            service.validatePose(startingPos, "TEST")
        }
        assertFalse(service.validatePose("test/test/test", "e"))
        assertFalse(service.validatePose("8/8/8/8/8//8// gt", "e"))
    }


    @Test
    fun `validatePose returns true when expected`() {
        val service = getInstance<ChessService>()

        assertTrue {
            service.validatePose(startingPos, "e2e4")
        }

        assertTrue {
            service.validatePose(startingPos, "d2d3")
        }

        assertTrue {
            service.validatePose(startingPos, "d2d3")
        }
    }

    @Test
    fun `validatePose returns false when is under mate`() {
        val service = getInstance<ChessService>()
        assertFalse(service.validatePose("1k5Q/7Q/8/8/8/8/8/1K6 b - - 0 1"))
        assertFalse(service.validatePose("1k6/Q1Q5/8/8/8/8/8/1K6 b - - 0 1"))
        assertFalse(service.validatePose("1k6/QQ6/8/8/8/8/8/1K6 b - - 0 1"))
    }

    @Test
    fun `validatePose returns false when it insufficient material`() {
        val service = getInstance<ChessService>()
        assertFalse(service.validatePose("1k6/8/8/8/8/8/8/1K6 b - - 0 1"))
        assertFalse(service.validatePose("1k6/8/8/8/8/8/8/8 b - - 0 1"))
    }

    @Test
    fun `validatePose returns false when move it's legal for another side`() {
        val service = getInstance<ChessService>()
        assertFalse(service.validatePose("1k6/7p/8/8/8/8/7P/1K6 w - - 0 1", "b8a8"))
    }

    @Test
    fun `validatePose returns false when bad move is later in sequence`() {
        val service = getInstance<ChessService>()
        assertFalse(service.validatePose(startingPos, "e2e4", "c7c5", "d2d4", "c5d4", "a1c1"))
    }
}