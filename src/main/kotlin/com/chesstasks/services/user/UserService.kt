package com.chesstasks.services.user

import com.chesstasks.data.dao.LoginCounterDao
import com.chesstasks.data.dao.UserDao
import com.chesstasks.data.dto.SimpleUserDto
import com.chesstasks.data.dto.UserDto
import org.koin.core.annotation.Single

@Single
class UserService(private val userDao: UserDao, private val loginCounterDao: LoginCounterDao) {
    suspend fun getById(id: Int): UserDto? = userDao.getById(id)
    suspend fun getByLogin(login: String): UserDto? = userDao.getByLogin(login)

    suspend fun getNewUsers(limit: Int = DEFAULT_NEW_USERS_LIMIT, skip: Long): List<SimpleUserDto> = userDao.getNewUsers(limit, skip)

    companion object {
        const val DEFAULT_NEW_USERS_LIMIT = 50
        const val DEFAULT_SEARCH_LIMIT = 50
        const val BAN_WHEN_LOGIN_COUNTER_GREATER_THAN = 10
    }

    suspend fun tryCreateUser(username: String, emailAddress: String, passwordHash: String): UserDto? = userDao.insertValues(username, emailAddress, passwordHash)
    suspend fun isValuesUnique(username: String, emailAddress: String): Boolean = userDao.isValuesUnique(username, emailAddress)
    suspend fun searchUserByUsername(query: String, limit: Int = DEFAULT_SEARCH_LIMIT, skip: Long = 0L): List<SimpleUserDto> = userDao.searchUserByUsername(query, limit, skip)

    suspend fun isAccountBlocked(userId: Int): Boolean {
        val counter = loginCounterDao.getCounterValue(userId) ?: loginCounterDao.insertEmpty(userId).counter
        return counter > BAN_WHEN_LOGIN_COUNTER_GREATER_THAN
    }

    suspend fun resetLoginCounter(userId: Int): Boolean {
        return loginCounterDao.resetCounterValue(userId)
    }

    suspend fun deleteUser(userId: Int): Boolean {
        return userDao.deleteUser(userId)
    }
}