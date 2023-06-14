package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.FriendDto
import com.chesstasks.data.dto.Friends
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

@Single
class FriendDao {
    suspend fun getById(id: Int): FriendDto? {
        return dbQuery {
            Friends.select { Friends.id eq id }.map(FriendDto::from).singleOrNull()
        }
    }

    suspend fun getFriends(userId: Int, limit: Int, skip: Long): List<FriendDto> = dbQuery {
        Friends
            .select { (Friends.userId eq userId) or (Friends.secondUserId eq userId) }
            .limit(limit, skip)
            .map(FriendDto::from)
    }

    suspend fun getFriend(userId: Int, anotherUserId: Int): FriendDto? = dbQuery {
        Friends.select {
            ((Friends.userId eq userId) or (Friends.secondUserId eq userId)) and
            ((Friends.secondUserId eq anotherUserId) or (Friends.userId eq anotherUserId))
        }.map(FriendDto::from).singleOrNull()
    }

    suspend fun areTheyFriends(userId: Int, anotherUserId: Int): Boolean = dbQuery {
        Friends.select {
            ((Friends.userId eq userId) or (Friends.secondUserId eq userId)) and
                    ((Friends.secondUserId eq anotherUserId) or (Friends.userId eq anotherUserId))
        }.count() > 0
    }

    suspend fun insertValues(userId: Int, secondUserId: Int): FriendDto? = dbQuery {
        Friends.insert {
            it[Friends.userId] = userId
            it[Friends.secondUserId] = secondUserId
        }.resultedValues?.map(FriendDto::from)?.singleOrNull()
    }

    suspend fun deleteFriendByIdAndUserId(id: Int, userId: Int): Boolean = dbQuery {
        Friends.deleteWhere {
            (Friends.id eq id) and ((Friends.userId eq userId) or (Friends.secondUserId eq userId))
        } > 0
    }
}