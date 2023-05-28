package com.chesstasks.services.authentication

import com.chesstasks.data.dto.UserDto
import com.chesstasks.security.PasswordHasher
import com.chesstasks.services.token.TokenService
import com.chesstasks.services.user.UserService
import org.koin.core.annotation.Single

data class AuthResult(val userId: Int, val username: String, val emailAddress: String, val token: String)

@Single
class AuthenticationService(
    private val userService: UserService,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService
) {
    suspend fun tryAuthenticate(login: String, password: String): AuthResult? {
        val user = userService.getByLogin(login) ?: return null
        if (!passwordHasher.comparePasswords(password, user.passwordHash)) return null
        val token = tokenService.createToken(user.id) ?: return null
        val tokenString = tokenService.writeToken(token)
        return AuthResult(user.id, user.username, user.emailAddress, tokenString)
    }

    suspend fun tryAuthenticate(tokenString: String): UserDto? {
        val token = tokenService.readToken(tokenString) ?: return null
        val userId = tokenService.validateToken(token) ?: return null
        return userService.getById(userId)
    }
}