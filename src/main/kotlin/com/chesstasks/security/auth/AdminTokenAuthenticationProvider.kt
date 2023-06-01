package com.chesstasks.security.auth

import com.chesstasks.services.authentication.AuthenticationService
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent

@Single
class AdminTokenAuthenticationProvider : AuthenticationProvider(Config(providerName)){
    class Config(name: String) : AuthenticationProvider.Config(name)

    companion object {
        @JvmStatic
        val providerName = "admin_token_authentication_provider"
    }

    private fun forbid(): Nothing = throw ForbidException()

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = context.call.request.headers["Authorization"] ?: forbid()
        val authenticationService: AuthenticationService = KoinJavaComponent.get(AuthenticationService::class.java)
        val tokenAuthResult = authenticationService.tryAuthenticateAdmin(token) ?: forbid()
        context.principal(TokenPrincipal(tokenAuthResult.token, tokenAuthResult.user))
    }
}

fun Route.admin(act: Route.() -> Unit) {
    authenticate(AdminTokenAuthenticationProvider.providerName) {
        act(this)
    }
}