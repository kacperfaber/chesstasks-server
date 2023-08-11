package com.chesstasks.services.friend

import com.chesstasks.data.dao.FriendDao
import com.chesstasks.data.dao.FriendRequestDao
import com.chesstasks.data.dto.FriendDto
import com.chesstasks.data.dto.FriendRequestDto
import org.koin.core.annotation.Single

@Single
class FriendService(private val friendRequestDao: FriendRequestDao, private val friendDao: FriendDao) {
    suspend fun sendRequest(userId: Int, targetId: Int): FriendRequestDto? {
        try {
            // Require that can't create two requests between the same people
            if (friendRequestDao.countRequestsBetweenUsers(userId, targetId) > 0) return null

            // Are they already friends?
            if (friendDao.areTheyFriends(userId, targetId)) return null

            return friendRequestDao.insertValues(userId, targetId)
        }
        catch (e: Exception) {return null}
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
}