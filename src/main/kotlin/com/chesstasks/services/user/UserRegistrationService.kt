package com.chesstasks.services.user

import com.chesstasks.security.PasswordHasher
import com.chesstasks.services.email.verification.EmailVerificationCodeService
import com.chesstasks.services.email.verification.VerificationEmailSender
import com.chesstasks.services.user.preferences.UserPreferencesService
import org.koin.core.annotation.Single

@Single
class UserRegistrationService(
    private val userService: UserService,
    private val passwordHasher: PasswordHasher,
    private val emailVerificationCodeService: EmailVerificationCodeService,
    private val verificationEmailSender: VerificationEmailSender,
    private val userPreferencesService: UserPreferencesService
) {

    enum class RegistrationResult(val i: String) {
        Ok("ok"),
        CodeSent("code_sent"),
        Fail("fail")
    }

    enum class VerificationResult(val i: String) {
        Fail("fail"),
        Ok("ok")
    }

    suspend fun tryRegister(username: String, emailAddress: String, password: String): RegistrationResult {
        if (!userService.isValuesUnique(username, emailAddress)) return RegistrationResult.Fail
        val passwordHash = passwordHasher.hash(password)
        return register(username, emailAddress, passwordHash)
    }

    suspend fun register(username: String, emailAddress: String, passwordHash: String): RegistrationResult {
        val emailVerificationCode = emailVerificationCodeService.insertValues(emailAddress, username, passwordHash)
            ?: return RegistrationResult.Fail

        verificationEmailSender.sendVerificationEmail(emailAddress, emailVerificationCode.code)

        return RegistrationResult.CodeSent
    }

    private suspend fun registerWithoutEmailVerification(username: String, emailAddress: String, passwordHash: String): RegistrationResult {
        val user = userService.tryCreateUser(username, emailAddress, passwordHash) ?: return RegistrationResult.Fail
        setupUserPrefs(user.id)
        return RegistrationResult.Ok
    }

    suspend fun tryVerify(emailAddress: String, code: String): VerificationResult {
        val emailVerificationCode = emailVerificationCodeService.getByEmailAndCode(emailAddress, code) ?: return VerificationResult.Fail
        val user = userService.tryCreateUser(emailVerificationCode.username, emailVerificationCode.emailAddress, emailVerificationCode.passwordHash) ?: return VerificationResult.Fail
        emailVerificationCodeService.deleteById(emailVerificationCode.id)
        setupUserPrefs(user.id)
        return VerificationResult.Ok
    }

    private suspend fun setupUserPrefs(userId: Int) {
        userPreferencesService.setupDefault(userId)
    }

    suspend fun tryRegisterAsAdmin(username: String, emailAddress: String, password: String, skipVerification: Boolean): RegistrationResult {
        if (!userService.isValuesUnique(username, emailAddress)) return RegistrationResult.Fail
        val passwordHash = passwordHasher.hash(password)

        if (skipVerification) {
            return registerWithoutEmailVerification(username, emailAddress, passwordHash)
        }

        return register(username, emailAddress, passwordHash)
    }
}