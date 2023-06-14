package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.FriendRequestDto
import com.chesstasks.data.dto.FriendRequests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class FriendRequestDao {
    suspend fun getById(id: Int): FriendRequestDto? = dbQuery {
        FriendRequests.select {FriendRequests.id eq id}.map(FriendRequestDto::from).singleOrNull()
    }

    suspend fun getBySenderAndTarget(senderId: Int, targetId: Int): FriendRequestDto? = dbQuery {
        FriendRequests.select { (FriendRequests.senderId eq senderId) and (FriendRequests.targetId eq targetId) }
            .map(FriendRequestDto::from)
            .singleOrNull()
    }

    suspend fun getBySender(senderId: Int, limit: Int, skip: Long): List<FriendRequestDto> = dbQuery {
        FriendRequests.select { (FriendRequests.senderId eq senderId) }
            .limit(limit, skip)
            .orderBy(FriendRequests.createdAt)
            .map(FriendRequestDto::from)
    }

    suspend fun getByTarget(targetId: Int, limit: Int, skip: Long): List<FriendRequestDto> = dbQuery {
        FriendRequests.select { (FriendRequests.targetId eq targetId) }
            .limit(limit, skip)
            .orderBy(FriendRequests.createdAt)
            .map(FriendRequestDto::from)
    }

    suspend fun insertValues(senderId: Int, targetId: Int): Int = dbQuery {
        FriendRequests.insert {
            it[FriendRequests.senderId] = senderId
            it[FriendRequests.targetId] = targetId
        } get FriendRequests.id
    }
}