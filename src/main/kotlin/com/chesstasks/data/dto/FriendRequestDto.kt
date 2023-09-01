package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object FriendRequests : BaseTable("friend_requests") {
    val senderId = integer("sender_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val targetId = integer("target_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}

class FriendRequestDto(
    id: Int,
    val senderId: Int,
    val targetId: Int,
    createdAt: Long
) : BaseDto(id, createdAt) {
    companion object {
        fun from(row: ResultRow): FriendRequestDto {
            return FriendRequestDto(
                row[FriendRequests.id],
                row[FriendRequests.senderId],
                row[FriendRequests.targetId],
                row[FriendRequests.createdAt]
            )
        }
    }
}