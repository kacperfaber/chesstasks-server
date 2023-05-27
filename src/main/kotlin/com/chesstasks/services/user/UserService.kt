package com.chesstasks.services.user

import com.chesstasks.data.dao.UserDao
import com.chesstasks.data.dto.UserDto
import org.koin.core.annotation.Single

@Single
class UserService(private val userDao: UserDao) {
    suspend fun getById(id: Int): UserDto? = userDao.getById(id)
    suspend fun getByLogin(login: String): UserDto? = userDao.getByLogin(login)
}