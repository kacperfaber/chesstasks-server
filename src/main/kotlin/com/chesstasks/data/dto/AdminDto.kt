package com.chesstasks.data.dto

import com.chesstasks.data.BaseTable

object Admins : BaseTable("admin") {
    val userId = integer("user_id").references(Users.id)
}

data class AdminDto(val id: Int, val createdAt: Long, val userId: Int)