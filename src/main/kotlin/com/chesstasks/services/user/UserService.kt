package com.chesstasks.services.user

import com.chesstasks.data.dao.UserDao
import com.chesstasks.data.dto.UserDto

class UserService(private val userDao: UserDao) {
    suspend fun getById(id: Int): UserDto? = userDao.getById(id)
    suspend fun getByLogin(login: String): UserDto? = userDao.getByLogin(login)
}