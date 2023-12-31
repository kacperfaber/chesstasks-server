package com.chesstasks.controllers.api.friend

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.friend.FriendService
import io.ktor.server.application.*
import io.ktor.server.request.*
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

        delete("/friend/by-id/{id}") {
            val userId = call.requirePrincipalId()
            val id = call.parameters["id"]?.toIntOrNull() ?: throw MissingQueryParameter("id")
            call.ofBoolean(friendService.deleteFriend(id, userId))
        }

        put("/friend/requests") {
            val currentId = call.requirePrincipalId()
            val targetUserId = call.receive<PutFriendRequestPayload>().userId
            call.ofNullable(friendService.sendRequest(currentId, targetUserId))
        }

        post("/friend/request/by-sender-id/{senderId}/accept") {
            val senderId = call.parameters["senderId"]?.toIntOrNull() ?: throw MissingQueryParameter("senderId")
            call.ofNullable(friendService.acceptRequestBySenderId(senderId, call.requirePrincipalId()))
        }

        post("/friend/request/by-sender-id/{senderId}/reject") {
            val senderId = call.parameters["senderId"]?.toIntOrNull() ?: throw MissingQueryParameter("senderId")
            call.ofBoolean(friendService.rejectRequestBySenderId(senderId, call.requirePrincipalId()))
        }

        get("/friend/all/include-user") {
            call.ofNullable(friendService.getFriendsIncludeUser(call.requirePrincipalId(), call.getSkip()))
        }

        get("/friend/requests/received/include-user") {
            call.ofNullable(friendService.getReceivedRequestsIncludeUser(call.requirePrincipalId(), call.getSkip()))
        }

        get("/friend/requests/sent/include-user") {
            call.ofNullable(friendService.getSentRequestsIncludeUser(call.requirePrincipalId(), call.getSkip()))
        }
    }
}