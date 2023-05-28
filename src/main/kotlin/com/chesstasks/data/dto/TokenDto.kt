package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import java.util.*

object Tokens : BaseTable("tokens") {
    val secret = text("secret").clientDefault { UUID.randomUUID().toString() }
    val userId = integer("user_id").references(Users.id)
}

class TokenDto(id: Int, createdAt: Long, val secret: String, val userId: Int) : BaseDto(id, createdAt)