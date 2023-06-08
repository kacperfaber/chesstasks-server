package com.chesstasks.security.auth.session

import com.chesstasks.services.user.UserService
import io.ktor.server.auth.*
import io.ktor.server.response.*
import org.koin.java.KoinJavaComponent.inject

internal const val USER_SESSION_AUTH_NAME = "user_ui"

fun AuthenticationConfig.configUserSession() {
    val userService: UserService by inject(UserService::class.java)

    session<UserSession>(USER_SESSION_AUTH_NAME) {
        validate { sess ->
            if (userService.getById(sess.id) != null) sess else null
        }

        challenge {
            call.respondRedirect("/ui/login")
        }
    }
}