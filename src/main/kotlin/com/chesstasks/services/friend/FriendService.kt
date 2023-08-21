package com.chesstasks.services.friend

import com.chesstasks.data.dao.FriendDao
import com.chesstasks.data.dao.FriendRequestDao
import com.chesstasks.data.dao.UserDao
import com.chesstasks.data.dto.FriendDto
import com.chesstasks.data.dto.FriendRequestDto
import org.koin.core.annotation.Single

@Single
class FriendService(
    private val friendRequestDao: FriendRequestDao,
    private val friendDao: FriendDao,
    private val userDao: UserDao
) {
    suspend fun sendRequest(userId: Int, targetId: Int): FriendRequestDto? {
        try {
            // Require that can't create two requests between the same people
            if (friendRequestDao.countRequestsBetweenUsers(userId, targetId) > 0) return null

            // Are they already friends?
            if (friendDao.areTheyFriends(userId, targetId)) return null

            return friendRequestDao.insertValues(userId, targetId)
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        const val DEFAULT_REQUEST_LIMIT = 50
        const val DEFAULT_FRIEND_LIMIT = 50
    }

    suspend fun getSentRequests(userId: Int, skip: Long): List<FriendRequestDto> {
        return friendRequestDao.getBySender(userId, DEFAULT_REQUEST_LIMIT, skip)
    }

    suspend fun getReceivedRequests(userId: Int, skip: Long): List<FriendRequestDto> {
        return friendRequestDao.getByTarget(userId, DEFAULT_REQUEST_LIMIT, skip)
    }

    suspend fun acceptRequestBySenderId(senderId: Int, authorizedUserId: Int): FriendDto? {
        // TODO: Actually only deleting the FriendRequest and creating a Friend.

        if (!friendRequestDao.deleteByRequestSenderIdAndTargetUserId(senderId, authorizedUserId)) return null

        return friendDao.insertValues(senderId, authorizedUserId)
    }

    suspend fun rejectRequestBySenderId(senderId: Int, authorizedUserId: Int): Boolean {
        // TODO: Actually only deleting the FriendRequest. It should make it 'rejected'
        return friendRequestDao.deleteByRequestSenderIdAndTargetUserId(senderId, authorizedUserId)
    }

    suspend fun getFriends(userId: Int, skip: Long): List<FriendDto> {
        return friendDao.getFriends(userId, DEFAULT_FRIEND_LIMIT, skip)
    }

    suspend fun deleteFriend(id: Int, authorizedUserId: Int): Boolean {
        return friendDao.deleteFriendByIdAndUserId(id, authorizedUserId)
    }

    suspend fun areTheyFriends(userId: Int, secondUserId: Int): Boolean {
        return friendDao.areTheyFriends(userId, secondUserId)
    }

    suspend fun getFriendsIncludeUser(userId: Int, skip: Long): List<FriendIncludeUserNames> {
        return friendDao.getFriends(userId, DEFAULT_FRIEND_LIMIT, skip).map { friend ->
            val userName = userDao.getNameById(friend.userId)
            val secUserName = userDao.getNameById(friend.secondUserId)

            FriendIncludeUserNames(
                id = friend.id,
                userId = friend.userId,
                secondUserId = friend.secondUserId,
                userName = userName,
                secondUserName = secUserName,
                createdAt = friend.createdAt
            )
        }
    }

    private fun List<FriendRequestDto>.includeUserNames(): List<FriendRequestIncludeUserNames> {
        return this.map { friend ->
            val senderName = userDao.getNameById(friend.senderId)
            val targetName = userDao.getNameById(friend.targetId)

            FriendRequestIncludeUserNames(
                id = friend.id,
                senderUserName = senderName,
                targetUserName = targetName,
                targetId = friend.targetId,
                senderId = friend.senderId,
                createdAt = friend.createdAt
            )
        }
    }

    suspend fun getSentRequestsIncludeUser(userId: Int, skip: Long): List<FriendRequestIncludeUserNames> {
        return friendRequestDao.getBySender(userId, DEFAULT_REQUEST_LIMIT, skip).includeUserNames()
    }

    suspend fun getReceivedRequestsIncludeUser(userId: Int, skip: Long): List<FriendRequestIncludeUserNames> {
        return friendRequestDao.getByTarget(userId, DEFAULT_REQUEST_LIMIT, skip).includeUserNames()
    }
}