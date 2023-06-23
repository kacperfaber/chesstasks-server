package com.chesstasks.services.user

import com.chesstasks.security.PasswordHasher
import com.chesstasks.services.email.verification.EmailVerificationCodeService
import org.koin.core.annotation.Single

@Single
class UserRegistrationService(
    private val userService: UserService,
    private val passwordHasher: PasswordHasher,
    private val emailVerificationCodeService: EmailVerificationCodeService
) {

    enum class RegistrationResult(val i: String) {
        CodeSent("code_sent"),
        Fail("fail")
    }

    suspend fun tryRegister(username: String, emailAddress: String, password: String): RegistrationResult {
        // TODO: Validate username and emailAddress unique with Users.

        val passwordHash = passwordHasher.hash(password)
        val emailVerificationCode = emailVerificationCodeService.insertValues(emailAddress, username, passwordHash)
            ?: return RegistrationResult.Fail

        // TODO: Try sent code...

        return RegistrationResult.CodeSent
    }

}