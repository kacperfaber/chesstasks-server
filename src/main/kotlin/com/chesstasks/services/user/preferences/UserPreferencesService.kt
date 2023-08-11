package com.chesstasks.services.user.preferences

import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.UserPreferencesDao
import com.chesstasks.services.friend.FriendService
import org.koin.core.annotation.Single

@Single
class UserPreferencesService(private val friendService: FriendService, private val userPreferencesDao: UserPreferencesDao) {
    suspend fun getHistoryVisibility(userId: Int): UserPuzzleHistoryVisibility? {
        return userPreferencesDao.getHistoryVisibility(userId)
    }

    suspend fun checkAccessToSeePuzzleHistory(userId: Int, authenticatedUserId: Int): Boolean {
        val historyVisibility =
            userPreferencesDao.getHistoryVisibility(userId) ?: throw Exception("No UserPreferences for user.id=$userId")
        return (userId == authenticatedUserId
                || historyVisibility == UserPuzzleHistoryVisibility.EVERYONE
                || historyVisibility == UserPuzzleHistoryVisibility.ONLY_FRIENDS && friendService.areTheyFriends(
            userId, authenticatedUserId
        ))
    }

    suspend fun checkAccessToSeeStatistics(userId: Int, authenticatedUserId: Int): Boolean {
        val statsV =
            userPreferencesDao.getStatisticsVisibility(userId) ?: throw Exception("No UserPreferences for user.id=$userId")
        return (userId == authenticatedUserId
                || statsV == UserStatisticsVisibility.EVERYONE
                || statsV == UserStatisticsVisibility.ONLY_FRIENDS && friendService.areTheyFriends(
            userId, authenticatedUserId
        ))
    }

    // TODO: Actually unused. I need to use it when user register.
    suspend fun setupDefault(userId: Int) {
        userPreferencesDao.insertValues(userId, UserPuzzleHistoryVisibility.ONLY_FRIENDS, UserStatisticsVisibility.ONLY_FRIENDS)
    }

    suspend fun setHistoryVisibility(userId: Int, value: UserPuzzleHistoryVisibility): Boolean {
        return userPreferencesDao.setHistoryVisibility(userId, value)
    }

    suspend fun getStatisticsVisibility(userId: Int) = userPreferencesDao.getStatisticsVisibility(userId)

    suspend fun setStatisticsVisibility(userId: Int, value: UserStatisticsVisibility) = userPreferencesDao.setStatisticsVisibility(userId, value)
}