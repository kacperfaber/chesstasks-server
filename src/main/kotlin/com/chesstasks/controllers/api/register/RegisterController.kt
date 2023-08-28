package com.chesstasks.controllers.api.register

import com.chesstasks.controllers.ofBoolean
import com.chesstasks.security.auth.apiKey
import com.chesstasks.services.user.UserRegistrationService
import io.ktor.server.application.*
import io.ktor.server.request.*
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
}