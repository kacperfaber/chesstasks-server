package com.chesstasks.services.user.preferences

import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dto.UserPreferencesDao
import org.koin.core.annotation.Single

@Single
class UserPreferencesService(private val userPreferencesDao: UserPreferencesDao) {
    suspend fun getHistoryVisibility(userId: Int): UserPuzzleHistoryVisibility? {
        return userPreferencesDao.getHistoryVisibility(userId)
    }

    suspend fun checkAccessToSeePuzzleHistory(userId: Int, authenticatedUserId: Int): Boolean {
        val historyVisibility = userPreferencesDao.getHistoryVisibility(userId) ?: throw Exception("No UserPreferences for user.id=$userId")
        return (userId == authenticatedUserId || historyVisibility == UserPuzzleHistoryVisibility.EVERYONE)
    }

    suspend fun setupDefault(userId: Int) {
        // TODO: Actually unused. I need to use it when user register.
        userPreferencesDao.insertValues(userId, UserPuzzleHistoryVisibility.ME)
    }
}