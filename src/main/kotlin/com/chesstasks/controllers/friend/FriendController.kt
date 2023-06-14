package com.chesstasks.controllers.friend

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.friend.FriendService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.friendController() {
    val friendService by inject<FriendService>(FriendService::class.java)

    user {
        get("/friend/all") {
            val userId = call.requirePrincipalId()
            call.ofNullable(friendService.getFriends(userId, call.getSkip()))
        }

        get("/friend/requests/sent") {
            val userId = call.requirePrincipalId()
            call.ofNullable(friendService.getSentRequests(userId, call.getSkip()))
        }

        get("/friend/requests/received") {
            val userId = call.requirePrincipalId()
            call.ofNullable(friendService.getReceivedRequests(userId, call.getSkip()))
        }

        // TODO: Test these 3 endpoints above

        delete("/friend/by-id/{id}") {
            val userId = call.requirePrincipalId()
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofBoolean(friendService.deleteFriend(id, userId))
        }
    }
}