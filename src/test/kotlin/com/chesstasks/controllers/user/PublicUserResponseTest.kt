package com.chesstasks.controllers.user

import com.chesstasks.data.dto.UserDto
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import testutils.BaseTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PublicUserResponseTest : BaseTest() {
    @Test
    fun `fromNullableUser returns NULL if given is NULL`() = testSuspend {
        assertNull(PublicUserResponse.fromNullableUser(null))
    }

    private fun sampleUser() =
        UserDto(0, System.currentTimeMillis(), "kacperfaber", "kacperf1234@gmail.com", "HelloWorld123")

    @Test
    fun `fromNullableUser returns INSTANCE if given is INSTANCE`() = testSuspend {
        assertNotNull(PublicUserResponse.fromNullableUser(sampleUser()))
    }

    @Test
    fun `fromNullableUser returns expected ID and USERNAME`() = testSuspend {
        val res = PublicUserResponse.fromNullableUser(sampleUser())!!
        assertEquals(0, res.id)
        assertEquals("kacperfaber", res.username)
    }
}