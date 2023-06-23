package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.UserDto
import com.chesstasks.data.dto.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

interface UserDao {
    suspend fun getById(id: Int): UserDto?
    suspend fun getByLogin(login: String): UserDto?
    suspend fun insertValues(username: String, emailAddress: String, passwordHash: String): UserDto?
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

    override suspend fun insertValues(username: String, emailAddress: String, passwordHash: String): UserDto? = dbQuery {
        Users.insert {
            it[Users.username] = username
            it[Users.emailAddress] = emailAddress
            it[Users.passwordHash] = passwordHash
        }.resultedValues?.map(UserDto::tryFrom)?.firstOrNull()
    }
}