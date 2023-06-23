package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.SimpleUserDto
import com.chesstasks.data.dto.UserDto
import com.chesstasks.data.dto.Users
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single

interface UserDao {
    suspend fun getById(id: Int): UserDto?
    suspend fun getByLogin(login: String): UserDto?
    suspend fun getNewUsers(limit: Int, skip: Long): List<SimpleUserDto>
}

@Single
class UserDaoImpl : UserDao {
    private fun resultRowToUser(resultRow: ResultRow): UserDto {
        return UserDto(
            id = resultRow[Users.id],
            username = resultRow[Users.username],
            passwordHash = resultRow[Users.passwordHash],
            emailAddress = resultRow[Users.emailAddress],
            createdAt = resultRow[Users.createdAt]
        )
    }

    override suspend fun getById(id: Int): UserDto? = dbQuery {
        Users.select { Users.id eq id }.map(::resultRowToUser).singleOrNull()
    }

    override suspend fun getByLogin(login: String): UserDto? = dbQuery {
        Users.select { (Users.emailAddress like login) or (Users.username eq login) }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getNewUsers(limit: Int, skip: Long): List<SimpleUserDto> = dbQuery {
        Users
            .slice(Users.id, Users.createdAt, Users.username)
            .selectAll()
            .limit(limit, skip)
            .orderBy(Users.createdAt, SortOrder.DESC)
            .map(SimpleUserDto::from)
    }
}