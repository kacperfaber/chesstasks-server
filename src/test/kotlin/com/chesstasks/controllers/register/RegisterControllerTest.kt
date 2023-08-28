package com.chesstasks.controllers.register

import com.chesstasks.data.dto.EmailVerificationCodeDto
import com.chesstasks.data.dto.EmailVerificationCodes
import com.chesstasks.data.dto.UserDto
import com.chesstasks.data.dto.Users
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


// TODO: RegisterControllerTest depends of 'properties.test.json'.

class RegisterControllerTest : BaseWebTest() {

    private fun setupUser() {
        transaction {
            Users.insert {
                it[id] = 0
                it[username] = "kacperfaber"
                it[emailAddress] = "kacperf1234@gmail.com"
                it[passwordHash] = "HelloWorld123"
            }
        }
    }

    private fun HttpRequestBuilder.registerBody(username: String = "kacperfaber", emailAddress: String = "kacperf1234@gmail.com", password: String = "HelloWorld123") {
        jsonBody("username" to username, "emailAddress" to emailAddress, "password" to password)
    }

    @Test
    fun `registerEndpoint returns FORBIDDEN if no api key given`() = testSuspend {
        app.client.post("/api/register") {registerBody();}.status.isForbid()
    }

    @Test
    fun `registerEndpoint returns FORBIDDEN if bad api key is given`() = testSuspend {
        app.client.post("/api/register"){registerBody(); withApiKey("jebac-disa")}.status.isForbid()
    }

    @Test
    fun `registerEndpoint returns BAD_REQUEST if email is taken`() = testSuspend {
        setupUser()
        app.client.post("/api/register") {registerBody(); withApiKey("secured-api-key")}.status.isBadRequest()
    }

    @Test
    fun `registerEndpoint returns BAD_REQUEST if email is invalid`() = testSuspend {
        val badEmails = "@gmail.com @test@com test@com test.com.com test.pl $$$.com $$$@gmail.com".split(" ")

        for (badEmail in badEmails) {
            app.client.post("/api/register") {registerBody(emailAddress = "@gmail.com"); withApiKey("secured-api-key")}.status.isBadRequest()
        }
    }

    @Test
    fun `registerEndpoint returns OK if data is OK`() = testSuspend {
        app.client.post("/api/register"){registerBody(); withApiKey("secured-api-key")}.status.isNoContent()
    }

    private fun userReg(id: Int): EmailVerificationCodeDto? {
        return transaction {
            EmailVerificationCodes
                .select { EmailVerificationCodes.id eq id }
                .map(EmailVerificationCodeDto::from)
                .firstOrNull()
        }
    }

    private fun firstUserReg(): EmailVerificationCodeDto? {
        return transaction {
            EmailVerificationCodes
                .selectAll()
                .map(EmailVerificationCodeDto::from)
                .firstOrNull()
        }
    }

    private fun countUserReg(): Long {
        return transaction {
            EmailVerificationCodes.selectAll().count()
        }
    }

    @Test
    fun `registerEndpoint makes UserRegistration row in database`() = testSuspend {
        val before = countUserReg()
        app.client.post("/api/register"){registerBody(); withApiKey("secured-api-key")}.status.isNoContent()
        assertEquals(before + 1, countUserReg())
    }

    @Test
    fun `registerEndpoint makes UserRegistration row in database with expected data`() = testSuspend {
        val username = "kacperfaber"
        val email = "kacperf1234@gmail.com"
        val pass = "HelloWorld123"

        app.client.post("/api/register"){ withApiKey("secured-api-key"); registerBody(username, email, pass)}.status.isNoContent()

        val reg = firstUserReg()
        assertEquals(email, reg?.emailAddress)
        assertEquals(username, reg?.username)
        assertEquals(pass, reg?.passwordHash)
        assertNotNull(reg?.code)
    }

    private fun HttpRequestBuilder.confirmBody(emailAddress: String = "kacperf1234@gmail.com", code: String = "ABC123") {
        jsonBody("emailAddress" to emailAddress, "code" to code)
    }

    @Test
    fun `confirmEndpoint returns FORBIDDEN if no api key given`() = testSuspend {
        app.client.post("/api/register/confirm") {confirmBody();}.status.isForbid()
    }

    @Test
    fun `confirmEndpoint returns FORBIDDEN if bad api key is given`() = testSuspend {
        app.client.post("/api/register/confirm") {confirmBody(); withApiKey("jebac-disa-kurwe")}.status.isForbid()
    }

    @Test
    fun `confirmEndpoint returns BAD_REQUEST if expected`() = testSuspend {
        app.client.post("/api/register/confirm"){confirmBody(); withApiKey("secured-api-key")}.status.isBadRequest()
    }

    private fun setupReg() = transaction {
        EmailVerificationCodes.insert {
            it[id] = 0
            it[emailAddress] = "kacperf1234@gmail.com"
            it[username] = "kacperfaber"
            it[passwordHash] = "HelloWorld123"
            it[code] = "ABC123"
        }
    }

    @Test
    fun `confirmEndpoint returns OK if expected`() = testSuspend {
        setupReg()
        app.client.post("/api/register/confirm"){confirmBody(); withApiKey("secured-api-key")}.status.isNoContent()
    }

    private fun countUsers(): Long = transaction {
        Users.selectAll().count()
    }

    @Test
    fun `confirmEndpoint returns OK and makes user record`() = testSuspend {
        setupReg()
        val bef = countUsers()
        app.client.post("/api/register/confirm"){confirmBody(); withApiKey("secured-api-key")}.status.isNoContent()

        assertEquals(bef + 1, countUsers())
    }

    private fun getFirstUser(): UserDto? = transaction {
        Users.selectAll().map(UserDto::tryFrom).firstOrNull()
    }

    @Test
    fun `confirmEndpoint returns OK and makes user record with expected data`() = testSuspend {
        setupReg()
        app.client.post("/api/register/confirm"){confirmBody(); withApiKey("secured-api-key")}.status.isNoContent()
        val u = getFirstUser()
        assertEquals("kacperf1234@gmail.com", u?.emailAddress)
        assertEquals("HelloWorld123", u?.passwordHash)
        assertEquals("kacperfaber", u?.username)
    }

    @Test
    fun `confirmEndpoint returns OK and deletes EmailVerificationRecord`() = testSuspend {
        setupReg()
        val bef = countUserReg()
        app.client.post("/api/register/confirm"){confirmBody(); withApiKey("secured-api-key")}.status.isNoContent()
        assertEquals(bef - 1, countUserReg())
    }
}