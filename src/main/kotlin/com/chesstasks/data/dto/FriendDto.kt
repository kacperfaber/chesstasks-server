package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object Friends : BaseTable("friends") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val secondUserId = integer("sec_user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}

class FriendDto(id: Int, val userId: Int, val secondUserId: Int, createdAt: Long) : BaseDto(id, createdAt) {
    companion object {
        fun from(resultRow: ResultRow): FriendDto = FriendDto(
            resultRow[Friends.id],
            resultRow[Friends.userId],
            resultRow[Friends.secondUserId],
            resultRow[Friends.createdAt]
        )
    }
}