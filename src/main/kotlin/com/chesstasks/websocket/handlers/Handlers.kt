package com.chesstasks.websocket.handlers

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.services.admin.AdminService
import com.chesstasks.websocket.Command
import io.ktor.server.auth.*
import io.ktor.server.websocket.*
import org.koin.java.KoinJavaComponent.inject

abstract class Handler {
    abstract suspend fun onReceived(session: DefaultWebSocketServerSession, command: Command)
    abstract suspend fun validate(session: DefaultWebSocketServerSession)
}

class LambdaHandler(val func: suspend DefaultWebSocketServerSession.(Command) -> Unit, val validateFun: suspend DefaultWebSocketServerSession.() -> Unit) : Handler() {
    override suspend fun onReceived(session: DefaultWebSocketServerSession, command: Command) {
        func(session, command)
    }

    override suspend fun validate(session: DefaultWebSocketServerSession) {
        validateFun(session)
    }
}

object Handlers {
    val handlers = mutableMapOf<String, Handler>()

    object Config {
        fun handle(s: String, func: suspend DefaultWebSocketServerSession.(Command) -> Unit) {
            handlers[s] = LambdaHandler(func) {}
        }
    }

    abstract class SecurityConfig {
        abstract suspend fun validate(sess: DefaultWebSocketServerSession)

        fun handle(s: String, func: suspend DefaultWebSocketServerSession.(Command) -> Unit) {
            handlers[s] = LambdaHandler(func, ::validate)
        }
    }

    object UserSecurityConfig : SecurityConfig() {
        override suspend fun validate(sess: DefaultWebSocketServerSession) {
            sess.call.principal<TokenPrincipal>()?.user?.id ?: throw Exception("This websocket command is forbidden")
        }
    }

    object AdminSecurityConfig : SecurityConfig() {
        private val adminService by inject<AdminService>(AdminService::class.java)

        override suspend fun validate(sess: DefaultWebSocketServerSession) {
            val userId =  sess.call.principal<TokenPrincipal>()?.user?.id ?: throw Exception("This websocket command is forbidden")
            if (!adminService.isUserAdmin(userId)) throw Exception("This websocket command is forbidden")
        }
    }
}

fun user(func: Handlers.SecurityConfig.() -> Unit) {
    func(Handlers.UserSecurityConfig)
}

fun admin(func: Handlers.SecurityConfig.() -> Unit) {
    func(Handlers.AdminSecurityConfig)
}