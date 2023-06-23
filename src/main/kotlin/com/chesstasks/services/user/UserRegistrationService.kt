package com.chesstasks.services.user

import com.chesstasks.security.PasswordHasher
import org.koin.core.annotation.Single

@Single
class UserRegistrationService(private val userService: UserService, private val passwordHasher: PasswordHasher) {

    enum class RegistrationResult(val i: String){
        CodeSent("code_sent"),
        Fail("fail")
    }

    suspend fun tryRegister(username: String, emailAddress: String, password: String): RegistrationResult {
        val passwordHash = passwordHasher.hash(password)
        val userDto = userService.tryCreateUser(username, emailAddress, passwordHash)
        // TODO: Try sent code...
        return if (userDto != null) RegistrationResult.CodeSent else RegistrationResult.Fail
    }

}