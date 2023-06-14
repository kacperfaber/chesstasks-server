package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.FriendRequestDto
import com.chesstasks.data.dto.FriendRequests
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

@Single
class FriendRequestDao {
    suspend fun getById(id: Int): FriendRequestDto? = dbQuery {
        FriendRequests.select { FriendRequests.id eq id }.map(FriendRequestDto::from).singleOrNull()
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

    suspend fun insertValues(senderId: Int, targetId: Int): FriendRequestDto? = dbQuery {
        FriendRequests.insert {
            it[FriendRequests.senderId] = senderId
            it[FriendRequests.targetId] = targetId
        }.resultedValues?.map(FriendRequestDto::from)?.singleOrNull()
    }

    suspend fun countRequestsBetweenUsers(user1: Int, user2: Int): Long = dbQuery {
        FriendRequests.select {
            ((FriendRequests.senderId eq user1) or (FriendRequests.senderId eq user2)) and ((FriendRequests.targetId eq user1) or (FriendRequests.targetId eq user2))
        }.count()
    }

    suspend fun deleteByRequestSenderIdAndTargetUserId(senderId: Int, targetUserId: Int): Boolean = dbQuery {
        FriendRequests.deleteWhere {
            (FriendRequests.senderId eq senderId) and (FriendRequests.targetId eq targetUserId)
        } > 0
    }
}