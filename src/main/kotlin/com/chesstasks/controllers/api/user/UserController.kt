package com.chesstasks.controllers.api.user

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.user.UserService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

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

    admin {
        delete("/user/as-admin/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofBoolean(userService.deleteUser(userId))
        }

        post("/user/as-admin/all/filtered") {
            val p = call.receive<FilteredUserListPayload>()
            val skip = call.getSkip()
            val limit = call.parameters["limit"]?.toIntOrNull() ?: UserService.DEFAULT_USERS_LIST_LIMIT
            call.ofNullable(userService.getFilteredList(p.usernameLike, p.emailLike, limit = limit, skip = skip))
        }

        get("/user/as-admin/by-id/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofNullable(userService.getById(id))
        }
    }
}