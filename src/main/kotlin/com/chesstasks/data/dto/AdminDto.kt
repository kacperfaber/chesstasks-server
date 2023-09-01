package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ReferenceOption

object Admins : BaseTable("admin") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}

class AdminDto(id: Int, createdAt: Long, val userId: Int) : BaseDto(id, createdAt)