package com.chesstasks.services.authentication

import com.chesstasks.data.dto.UserDto
import com.chesstasks.security.PasswordHasher
import com.chesstasks.services.admin.AdminService
import com.chesstasks.services.token.Token
import com.chesstasks.services.token.TokenService
import com.chesstasks.services.user.UserService
import org.koin.core.annotation.Single

data class AuthResult(val userId: Int, val username: String, val emailAddress: String, val token: String)

data class TokenAuthResult(val token: Token, val user: UserDto)

data class SessionAuthentication(val userId: Int, val username: String, val emailAddress: String, val isAdmin: Boolean)

@Single
class AuthenticationService(
    private val userService: UserService,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val adminService: AdminService
) {
    suspend fun tryAuthenticate(login: String, password: String): AuthResult? {
        val user = userService.getByLogin(login) ?: return null
        if (userService.isAccountBlocked(user.id)) return null
        val token = tokenService.createToken(user.id) ?: return null
        if (!passwordHasher.comparePasswords(password, user.passwordHash)) return null
        val tokenString = tokenService.writeToken(token)
        userService.resetLoginCounter(user.id)
        return AuthResult(user.id, user.username, user.emailAddress, tokenString)
    }

    suspend fun tryAuthenticate(tokenString: String): TokenAuthResult? {
        val token = tokenService.readToken(tokenString) ?: return null
        val userId = tokenService.validateToken(token) ?: return null
        val user = userService.getById(userId) ?: return null
        return TokenAuthResult(token, user)
    }

    suspend fun tryAuthenticateAdmin(tokenString: String): TokenAuthResult? {
        val tokenAuthResult = tryAuthenticate(tokenString) ?: return null
        return if (adminService.isUserAdmin(tokenAuthResult.user.id)) tokenAuthResult else null
    }

    suspend fun tryAuthenticateSession(login: String, password: String): SessionAuthentication? {
        val user = userService.getByLogin(login) ?: return null
        if (!passwordHasher.comparePasswords(password, user.passwordHash)) return null
        return SessionAuthentication(user.id, user.username, user.emailAddress, adminService.isUserAdmin(user.id))
    }
}