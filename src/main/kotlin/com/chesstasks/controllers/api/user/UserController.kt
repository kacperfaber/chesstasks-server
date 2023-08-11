package com.chesstasks.controllers.api.user

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.user.UserService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.get

fun Route.userController() {
    val userService by inject<UserService>(UserService::class.java)

    user {
        get("user/me") {
            call.ofNullable(userService.getById(call.requirePrincipalId()))
        }

        get("user/by-id/{id}") {
            // TODO: Use UserPreferences to permit or deny stranger access my profile.
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(PublicUserResponse.fromNullableUser(userService.getById(id)))
        }

        get("user/search/by-username") {
            val query = call.parameters["query"] ?: throw MissingQueryParameter("query")
            call.ofNullable(userService.searchUserByUsername(query, skip = call.getSkip()))
        }
    }
}