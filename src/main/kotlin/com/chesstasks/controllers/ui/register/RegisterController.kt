package com.chesstasks.controllers.ui.register

import com.chesstasks.controllers.getValue
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.services.user.UserRegistrationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

data class RegisterModel(val username: String, val emailAddress: String, val error: Boolean)

data class VerifyModel(val emailAddress: String, val error: Boolean)

data class RegisterFormData(val username: String, val emailAddress: String, val password: String)

data class VerifyFormData(val emailAddress: String, val code: String)

fun Parameters.toVerifyFormData(): VerifyFormData = VerifyFormData(getValue("emailAddress"), getValue("code"))

fun Parameters.toRegisterFormData(): RegisterFormData = RegisterFormData(
    username = getValue("username"),
    emailAddress = getValue("emailAddress"),
    password = getValue("password")
)

suspend fun ApplicationCall.toRegisterPage(username: String = "", emailAddress: String = "", error: Boolean = false) {
    respondRedirect("/ui/register?username=$username&emailAddress=$emailAddress&error=$error")
}

suspend fun ApplicationCall.toVerifyPage(emailAddress: String = "", error: Boolean = false) {
    respondRedirect("/ui/register/verify?email=$emailAddress&error=$error")
}

fun Route.registerController() {
    val userRegistrationService by inject<UserRegistrationService>()

    get("/ui/register/verify") {
        val emailAddress = call.parameters["email"] ?: throw MissingQueryParameter("email")
        val isError = call.parameters["error"]?.toBoolean() ?: false
        call.respond(FreeMarkerContent("register/verify.ftl", VerifyModel(emailAddress, isError)))
    }

    post("/ui/register/verify/submit") {
        val (emailAddress, code) = call.receiveParameters().toVerifyFormData()
        val result = userRegistrationService.tryVerify(emailAddress, code)

        if (result == UserRegistrationService.VerificationResult.Ok) {
            call.respondRedirect("/ui/login?login=${emailAddress}")
        } else {
            call.toVerifyPage(emailAddress, error = true)
        }
    }

    get("/ui/register") {
        val username = call.parameters["username"] ?: ""
        val emailAddress = call.parameters["email"] ?: ""
        val isError = call.parameters["error"]?.toBoolean() ?: false
        call.respond(FreeMarkerContent("register/register.ftl", RegisterModel(username, emailAddress, isError)))
    }

    post("/ui/register/submit") {
        val (username, emailAddress, password) = call.receiveParameters().toRegisterFormData()
        val result = userRegistrationService.tryRegister(username, emailAddress, password)

        if (result == UserRegistrationService.RegistrationResult.CodeSent) {
            call.toVerifyPage(emailAddress)
        } else {
            call.toRegisterPage(username, emailAddress, error = true)
        }
    }
}