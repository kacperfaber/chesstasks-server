package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import com.fasterxml.jackson.annotation.JsonIgnore

object Users : BaseTable("`user`") {
    val passwordHash = varchar("password_hash", 32)
    val username = varchar("username", 32)
    val emailAddress = varchar("email_address", 32)
}

class UserDto(id: Int, createdAt: Long, val username: String, val emailAddress: String, @JsonIgnore val passwordHash: String) :
    BaseDto(id, createdAt)