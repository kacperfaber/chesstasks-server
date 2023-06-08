package com.chesstasks.security.auth.session

import com.chesstasks.services.admin.AdminService
import io.ktor.server.auth.*
import io.ktor.server.response.*
import org.koin.java.KoinJavaComponent.inject

internal const val ADMIN_SESSION_AUTH_NAME = "admin_ui"

fun AuthenticationConfig.configAdminSession() {
    val adminService by inject<AdminService>(AdminService::class.java)

    session<UserSession>(ADMIN_SESSION_AUTH_NAME) {
        validate { sess ->
            if (adminService.isUserAdmin(sess.id)) sess else null
        }

        challenge {
            call.respondRedirect("/ui/login")
        }
    }
}