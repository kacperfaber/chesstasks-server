package com.chesstasks.services.user

import com.chesstasks.data.dao.UserDao
import com.chesstasks.data.dto.SimpleUserDto
import com.chesstasks.data.dto.UserDto
import org.koin.core.annotation.Single

@Single
class UserService(private val userDao: UserDao) {
    suspend fun getById(id: Int): UserDto? = userDao.getById(id)
    suspend fun getByLogin(login: String): UserDto? = userDao.getByLogin(login)

    suspend fun getNewUsers(limit: Int = DEFAULT_NEW_USERS_LIMIT, skip: Long): List<SimpleUserDto> = userDao.getNewUsers(limit, skip)

    companion object {
        const val DEFAULT_NEW_USERS_LIMIT = 50
    }

    suspend fun tryCreateUser(username: String, emailAddress: String, passwordHash: String): UserDto? = userDao.insertValues(username, emailAddress, passwordHash)
    suspend fun isValuesUnique(username: String, emailAddress: String): Boolean = userDao.isValuesUnique(username, emailAddress)
}