package com.chesstasks.websocket.handlers

import com.chesstasks.security.auth.TokenPrincipal
import com.chesstasks.services.admin.AdminService
import com.chesstasks.websocket.Command
import com.chesstasks.websocket.exceptions.CommandForbiddenException
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

class ExceptionHandler(val func: suspend DefaultWebSocketServerSession.(Exception) -> Unit)

object Handlers {
    val handlers = mutableMapOf<String, Handler>()
    val exceptionHandlers = mutableMapOf<Class<*>, ExceptionHandler>()

    suspend fun tryHandleException(session: DefaultWebSocketServerSession, e: Exception) {
        val key = exceptionHandlers.keys.firstOrNull {
            it.isAssignableFrom(e.javaClass)
        }

        (exceptionHandlers.getOrDefault(key, null) ?: throw e).func(session, e)
    }

    object Config {
        fun handle(s: String, func: suspend DefaultWebSocketServerSession.(Command) -> Unit) {
            handlers[s] = LambdaHandler(func) {}
        }

        inline fun <reified T : Exception> exception(noinline act: suspend DefaultWebSocketServerSession.(e: Exception) -> Unit) {
            exceptionHandlers[T::class.java] = ExceptionHandler(act)
        }
    }

    abstract class SecurityConfig {
        abstract suspend fun validate(sess: DefaultWebSocketServerSession)

        fun handle(s: String, func: suspend DefaultWebSocketServerSession.(Command) -> Unit) {
            handlers[s] = LambdaHandler(func, ::validate)
        }

        inline fun <reified T : Exception> exception(noinline act: suspend DefaultWebSocketServerSession.(e: Exception) -> Unit) {
            exceptionHandlers[T::class.java] = ExceptionHandler(act)
        }
    }

    object UserSecurityConfig : SecurityConfig() {
        override suspend fun validate(sess: DefaultWebSocketServerSession) {
            sess.call.principal<TokenPrincipal>()?.user?.id ?: throw CommandForbiddenException()
        }
    }

    object AdminSecurityConfig : SecurityConfig() {
        private val adminService by inject<AdminService>(AdminService::class.java)

        override suspend fun validate(sess: DefaultWebSocketServerSession) {
            val userId =  sess.call.principal<TokenPrincipal>()?.user?.id ?: throw CommandForbiddenException()
            if (!adminService.isUserAdmin(userId)) throw CommandForbiddenException()
        }
    }
}

fun user(func: Handlers.SecurityConfig.() -> Unit) {
    func(Handlers.UserSecurityConfig)
}

fun admin(func: Handlers.SecurityConfig.() -> Unit) {
    func(Handlers.AdminSecurityConfig)
}