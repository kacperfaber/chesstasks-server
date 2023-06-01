package com.chesstasks.services.admin

import com.chesstasks.data.dao.AdminDao
import com.chesstasks.data.dto.AdminDto
import org.koin.core.annotation.Single

@Single
class AdminService(private val adminDao: AdminDao) {
    suspend fun getByUserId(userId: Int): AdminDto? {
        return adminDao.getByUserId(userId)
    }

    suspend fun getById(id: Int): AdminDto? {
        return adminDao.getById(id)
    }
}