package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable

object Admins : BaseTable("admin") {
    val userId = integer("user_id").references(Users.id)
}

class AdminDto(id: Int, createdAt: Long, val userId: Int) : BaseDto(id, createdAt)