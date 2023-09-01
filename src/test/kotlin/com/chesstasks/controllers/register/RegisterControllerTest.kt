package com.chesstasks.controllers.register

import com.chesstasks.data.dto.*
import com.chesstasks.services.user.UserRegistrationService
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
import kotlin.test.assertNull


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

    private fun setupAdmin() = transaction {
        Admins.insert {
            it[userId] = 0
            it[id] = 0
        }
    }

    private fun HttpRequestBuilder.registerBody(
        username: String = "kacperfaber",
        emailAddress: String = "kacperf1234@gmail.com",
        password: String = "HelloWorld123"
    ) {
        jsonBody("username" to username, "emailAddress" to emailAddress, "password" to password)
    }

    @Test
    fun `registerEndpoint returns FORBIDDEN if no api key given`() = testSuspend {
        app.client.post("/api/register") { registerBody(); }.status.isForbid()
    }

    @Test
    fun `registerEndpoint returns FORBIDDEN if bad api key is given`() = testSuspend {
        app.client.post("/api/register") { registerBody(); withApiKey("jebac-disa") }.status.isForbid()
    }

    @Test
    fun `registerEndpoint returns BAD_REQUEST if email is taken`() = testSuspend {
        setupUser()
        app.client.post("/api/register") { registerBody(); withApiKey("secured-api-key") }.status.isBadRequest()
    }

    @Test
    fun `registerEndpoint returns BAD_REQUEST if email is invalid`() = testSuspend {
        val badEmails = "@gmail.com @test@com test@com test.com.com test.pl $$$.com $$$@gmail.com".split(" ")

        for (badEmail in badEmails) {
            app.client.post("/api/register") { registerBody(emailAddress = "@gmail.com"); withApiKey("secured-api-key") }.status.isBadRequest()
        }
    }

    @Test
    fun `registerEndpoint returns OK if data is OK`() = testSuspend {
        app.client.post("/api/register") { registerBody(); withApiKey("secured-api-key") }.status.isNoContent()
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
        app.client.post("/api/register") { registerBody(); withApiKey("secured-api-key") }.status.isNoContent()
        assertEquals(before + 1, countUserReg())
    }

    @Test
    fun `registerEndpoint makes UserRegistration row in database with expected data`() = testSuspend {
        val username = "kacperfaber"
        val email = "kacperf1234@gmail.com"
        val pass = "HelloWorld123"

        app.client.post("/api/register") {
            withApiKey("secured-api-key"); registerBody(
            username,
            email,
            pass
        )
        }.status.isNoContent()

        val reg = firstUserReg()
        assertEquals(email, reg?.emailAddress)
        assertEquals(username, reg?.username)
        assertEquals(pass, reg?.passwordHash)
        assertNotNull(reg?.code)
    }

    private fun HttpRequestBuilder.confirmBody(
        emailAddress: String = "kacperf1234@gmail.com",
        code: String = "ABC123"
    ) {
        jsonBody("emailAddress" to emailAddress, "code" to code)
    }

    @Test
    fun `confirmEndpoint returns FORBIDDEN if no api key given`() = testSuspend {
        app.client.post("/api/register/confirm") { confirmBody(); }.status.isForbid()
    }

    @Test
    fun `confirmEndpoint returns FORBIDDEN if bad api key is given`() = testSuspend {
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("jebac-disa-kurwe") }.status.isForbid()
    }

    @Test
    fun `confirmEndpoint returns BAD_REQUEST if expected`() = testSuspend {
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("secured-api-key") }.status.isBadRequest()
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
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("secured-api-key") }.status.isNoContent()
    }

    private fun countUsers(): Long = transaction {
        Users.selectAll().count()
    }

    @Test
    fun `confirmEndpoint returns OK and makes user record`() = testSuspend {
        setupReg()
        val bef = countUsers()
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("secured-api-key") }.status.isNoContent()

        assertEquals(bef + 1, countUsers())
    }

    private fun getFirstUser(): UserDto? = transaction {
        Users.selectAll().map(UserDto::tryFrom).firstOrNull()
    }

    @Test
    fun `confirmEndpoint returns OK and makes user record with expected data`() = testSuspend {
        setupReg()
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("secured-api-key") }.status.isNoContent()
        val u = getFirstUser()
        assertEquals("kacperf1234@gmail.com", u?.emailAddress)
        assertEquals("HelloWorld123", u?.passwordHash)
        assertEquals("kacperfaber", u?.username)
    }

    @Test
    fun `confirmEndpoint returns OK and deletes EmailVerificationRecord`() = testSuspend {
        setupReg()
        val bef = countUserReg()
        app.client.post("/api/register/confirm") { confirmBody(); withApiKey("secured-api-key") }.status.isNoContent()
        assertEquals(bef - 1, countUserReg())
    }

    @Test
    fun `registerAsAdminEndpoint returns FORBID if no authentication`() = testSuspend {
        setupUser()
        app.client.post("/api/register/as-admin") { }.status.isForbid()
    }

    @Test
    fun `registerAsAdminEndpoint returns FORBID if user but not admin`() = testSuspend {
        setupUser()
        app.client.post("/api/register/as-admin") { withToken(0) }.status.isForbid()
    }

    private fun HttpRequestBuilder.registerAsAdm(username: String = "Aneta", emailAddress: String = "aneta@gmail.com", password: String = "HelloWorld123", skipVerification: Boolean = false) {
        jsonBody(
            "username" to username,
            "emailAddress" to emailAddress,
            "password" to password,
            "skipVerification" to skipVerification
        )
    }

    @Test
    fun `registerAsAdminEndpoint returns OK if admin and body given`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm() }.status.isOk()
    }

    @Test
    fun `registerAsAdminEndpoint returns BAD_REQUEST if admin but username is taken`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(username = "kacperfaber") }.status.isBadRequest()
    }

    @Test
    fun `registerAsAdminEndpoint returns BAD_REQUEST if admin but emailAddress is taken`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(emailAddress = "kacperf1234@gmail.com") }.status.isBadRequest()
    }

    private fun getVerificationCodeByUsername(username: String = "Aneta"): EmailVerificationCodeDto? = transaction {
        EmailVerificationCodes.select {EmailVerificationCodes.username eq username}
            .map(EmailVerificationCodeDto::from)
            .singleOrNull()
    }

    @Test
    fun `registerAsAdminEndpoint returns OK and makes EmailVerificationCode in database if skipVerification=false`() = testSuspend {
        val username = "Anetka"
        val email = "anetka@gmailik.com"
        val pass = "helloWorld"

        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(username, email, pass) }.status.isOk()

        val code = getVerificationCodeByUsername(username)
        assertEquals(username, code?.username)
        assertEquals(email, code?.emailAddress)
        assertEquals(pass, code?.passwordHash)
    }

    @Test
    fun `registerAsAdminEndpoint returns OK and dont make EmailVerificationCode in database if skipVerification=true`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(username = "Aneta", skipVerification = true) }.status.isOk()

        val code = getVerificationCodeByUsername("Aneta")
        assertNull(code)
    }

    private fun getUserByUsername(username: String = "Aneta"): UserDto? = transaction {
        Users.select {Users.username eq username}
            .map(UserDto::tryFrom)
            .singleOrNull()
    }

    @Test
    fun `registerAsAdminEndpoint returns OK and makes User in database if skipVerification=true`() = testSuspend {
        val username = "Anetka"
        val email = "anetka@gmailik.com"
        val pass = "helloWorld"

        setupUser()
        setupAdmin()
        app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(username, email, pass, skipVerification = true) }.status.isOk()

        val u = getUserByUsername(username)
        assertEquals(email, u?.emailAddress)
        assertEquals(pass, u?.passwordHash)
    }

    @Test
    fun `registerAsAdminEndpoint returns OK and body equals to 'RegistrationResult,CodeSent,name' if everything is alright and skipVerification=false`() = testSuspend {
        setupUser()
        setupAdmin()
        val r = app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(skipVerification = false) }
        r.status.isOk()
        r.jsonPath("$", UserRegistrationService.RegistrationResult.CodeSent.name)
    }

    @Test
    fun `registerAsAdminEndpoint returns OK and body equals to 'RegistrationResult,Ok,name' if everything is alright and skipVerification=false`() = testSuspend {
        setupUser()
        setupAdmin()
        val r = app.client.post("/api/register/as-admin") { withToken(0); registerAsAdm(skipVerification = true) }
        r.status.isOk()
        r.jsonPath("$", UserRegistrationService.RegistrationResult.Ok.name)
    }

    private fun saveToken(userId: Int = 0, secret: String = "abc") = transaction {
        Tokens.insert {
            it[Tokens.userId] = userId
            it[Tokens.secret] = secret
        }
    }

    @Test
    fun `registerAsAdminEndpoint returns BAD_REQUEST if emailAddress is invalid`() = testSuspend {
        setupUser()
        setupAdmin()
        saveToken()

        val emails = arrayOf("@gmail.com", "test.com", "kacperek.com", "jebacdisa@@.com")

        emails.forEach {
            app.client.post("/api/register/as-admin") { useToken(0, "abc"); registerAsAdm(emailAddress = it) }.status.isBadRequest()
        }
    }

    @Test
    fun `registerAsAdminEndpoint returns BAD_REQUEST if username is too short`() = testSuspend {
        setupUser()
        setupAdmin()
        saveToken()

        val usernames = arrayOf("$", "", "a", "1")

        usernames.forEach {
            app.client.post("/api/register/as-admin") { useToken(0, "abc"); registerAsAdm(username = it) }.status.isBadRequest()
        }
    }

    @Test
    fun `registerAsAdminEndpoint returns BAD_REQUEST if password is too short`() = testSuspend {
        setupUser()
        setupAdmin()
        saveToken()

        val passwords = arrayOf("$", "", "a", "1")

        passwords.forEach {
            app.client.post("/api/register/as-admin") { useToken(0, "abc"); registerAsAdm(password = it) }.status.isBadRequest()
        }
    }
}