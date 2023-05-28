package com.chesstasks.security.auth

import com.chesstasks.services.authentication.AuthenticationService
import io.ktor.server.auth.*
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.get

@Single
class TokenAuthenticationProvider : AuthenticationProvider(Config(providerName)){
    class Config(name: String) : AuthenticationProvider.Config(name)

    private fun getAuthorizationValue(context: AuthenticationContext): String? {
        return context.call.request.headers["Authorization"]
    }

    private fun unauthorized(): Nothing = throw UnauthorizedException()

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = getAuthorizationValue(context) ?: unauthorized()
        val authenticationService: AuthenticationService = get(AuthenticationService::class.java)
        val tokenAuthResult = authenticationService.tryAuthenticate(token) ?: unauthorized()
        context.principal(TokenPrincipal(tokenAuthResult.token, tokenAuthResult.user))
    }

    companion object {
        @JvmStatic
        val providerName = "token_auth_provider"
    }
}