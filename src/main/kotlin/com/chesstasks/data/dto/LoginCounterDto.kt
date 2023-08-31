package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object LoginCounters : BaseTable("login_counters"){
    val userId = integer("user_id").references(Users.id)
    val counter = integer("counter")
}

class LoginCounterDto(id: Int, val userId: Int, val counter: Int, createdAt: Long) : BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): LoginCounterDto = LoginCounterDto(
            r[LoginCounters.id],
            r[LoginCounters.userId],
            r[LoginCounters.counter],
            r[LoginCounters.createdAt]
        )
    }
}