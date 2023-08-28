package com.chesstasks.security.auth

import com.chesstasks.services.apikey.ApiKeyService
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single

@Single
class ApiKeyAuthenticationProvider(private val apiKeyService: ApiKeyService) : AuthenticationProvider(Config(providerName)){
    class Config(name: String) : AuthenticationProvider.Config(name)

    companion object {
        @JvmStatic
        val providerName = "api_key_authentication_provider"
    }

    private fun getAuthorizationValue(context: AuthenticationContext): String? {
        return context.call.request.headers["Authorization"]
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        try {
            val authHeader = getAuthorizationValue(context) ?: throw ForbidException()
            if (!apiKeyService.validateKey(authHeader)) {
                throw ForbidException();
            }
            context.principal(ApiKeyPrincipal())
        }

        catch (e: CloseProtocolException) {
            context.principal<ApiKeyPrincipal>(null)
        }
    }
}

fun Route.apiKey(act: Route.() -> Unit) {
    authenticate(ApiKeyAuthenticationProvider.providerName) {
        act(this)
    }
}