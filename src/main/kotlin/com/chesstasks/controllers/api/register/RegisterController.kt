package com.chesstasks.controllers.api.register

import com.chesstasks.controllers.ofBoolean
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.apiKey
import com.chesstasks.services.user.UserRegistrationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.registerController() {
    val userRegistrationService by inject<UserRegistrationService>(UserRegistrationService::class.java)

    apiKey {
        post("/register") {
            val (username, emailAddress, password) = call.receive<RegisterPayload>()
            val result = userRegistrationService.tryRegister(username, emailAddress, password)
            call.ofBoolean(result == UserRegistrationService.RegistrationResult.CodeSent)
        }

        post("/register/confirm") {
            val (emailAddress, code) = call.receive<ConfirmPayload>()
            val vRes = userRegistrationService.tryVerify(emailAddress, code)
            call.ofBoolean(vRes == UserRegistrationService.VerificationResult.Ok)
        }
    }

    admin {
        post("/register/as-admin") {
            val p = call.receive<RegisterAsAdminPayload>()
            val result = userRegistrationService.tryRegisterAsAdmin(p.username, p.emailAddress, p.password, p.skipVerification)
            val statusCode = if (result == UserRegistrationService.RegistrationResult.Fail) HttpStatusCode.BadRequest else HttpStatusCode.OK
            call.respond(statusCode, result)
        }
    }
}