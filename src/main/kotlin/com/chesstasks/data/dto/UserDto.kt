package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.sql.ResultRow

object Users : BaseTable("`user`") {
    val passwordHash = varchar("password_hash", 32)
    val username = varchar("username", 32).uniqueIndex()
    val emailAddress = varchar("email_address", 32).uniqueIndex()
}

class UserDto(
    id: Int,
    createdAt: Long,
    val username: String,
    val emailAddress: String,
    @JsonIgnore val passwordHash: String
) :
    BaseDto(id, createdAt) {
    companion object {
        fun tryFrom(row: ResultRow): UserDto? {
            return try {
                UserDto(
                    row[Users.id],
                    row[Users.createdAt],
                    row[Users.username],
                    row[Users.emailAddress],
                    row[Users.passwordHash]
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

class SimpleUserDto(val id: Int, val createdAt: Long, val username: String) {
    companion object {
        fun from(r: ResultRow): SimpleUserDto {
            return SimpleUserDto(r[Users.id], r[Users.createdAt], r[Users.username])
        }
    }
}