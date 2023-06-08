package com.chesstasks.controllers

import com.chesstasks.exceptions.BadRequestException
import com.chesstasks.security.auth.TokenPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*

suspend inline fun <reified T> ApplicationCall.ofNullable(value: T?) {
    respond(HttpStatusCode.OK, value ?: throw BadRequestException())
}

suspend fun ApplicationCall.ofBoolean(value: Boolean, whenTrue: HttpStatusCode = HttpStatusCode.NoContent) {
    respond(if (value) whenTrue else HttpStatusCode.BadRequest)
}

fun ApplicationCall.requirePrincipalId(): Int {
    return principal<TokenPrincipal>()?.user?.id ?: throw Exception("No principal")
}

suspend fun ApplicationCall.view(name: String, ext: String = ".ftl", model: Any) {
    respond(FreeMarkerContent("$name$ext", model))
}

suspend inline fun ApplicationCall.view(name: String, ext: String = ".ftl", act: () -> Any) {
    respond(FreeMarkerContent("$name$ext", act()))
}