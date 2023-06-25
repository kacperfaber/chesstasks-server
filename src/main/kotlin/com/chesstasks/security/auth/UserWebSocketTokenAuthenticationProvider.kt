package com.chesstasks.security.auth

import com.chesstasks.services.authentication.AuthenticationService
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent

class CloseProtocolException : Exception()

@Single
class UserWebSocketTokenAuthenticationProvider : AuthenticationProvider(Config(providerName)){
    class Config(name: String) : AuthenticationProvider.Config(name)

    companion object {
        @JvmStatic
        val providerName = "websocket_user_token_authentication_provider"
    }

    private fun protocolError(): Nothing = throw CloseProtocolException()

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        try {
            val token = context.call.request.headers["Authorization"] ?: protocolError()
            val authenticationService: AuthenticationService = KoinJavaComponent.get(AuthenticationService::class.java)
            val tokenAuthResult = authenticationService.tryAuthenticateAdmin(token) ?: protocolError()
            context.principal(TokenPrincipal(tokenAuthResult.token, tokenAuthResult.user))
        }

        catch (e: CloseProtocolException) {
            context.principal<TokenPrincipal>(null)
        }
    }
}

fun Route.webSocketUser(act: Route.() -> Unit) {
    authenticate(UserWebSocketTokenAuthenticationProvider.providerName) {
        act(this)
    }
}