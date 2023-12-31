package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.AdminDto
import com.chesstasks.data.dto.Admins
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

interface AdminDao {
    suspend fun getByUserId(userId: Int): AdminDto?
    suspend fun getById(id: Int): AdminDto?
}

@Single
class AdminDaoImpl : AdminDao {
    private fun resultRowToAdmin(resultRow: ResultRow): AdminDto {
        return AdminDto(resultRow[Admins.id], resultRow[Admins.createdAt], resultRow[Admins.userId])
    }

    override suspend fun getByUserId(userId: Int): AdminDto? {
        return dbQuery {
            val row = Admins.select { Admins.userId eq userId }.singleOrNull() ?: return@dbQuery null
            resultRowToAdmin(row)
        }
    }

    override suspend fun getById(id: Int): AdminDto? {
        return dbQuery {
            val row = Admins.select(Admins.id eq id).singleOrNull() ?: return@dbQuery null
            resultRowToAdmin(row)
        }
    }
}