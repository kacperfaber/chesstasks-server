package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object Openings : BaseTable("openings") {
    val moves = varchar("moves", 256)
    val eco = varchar("eco", 3)
    val name = varchar("name", 256)
}

class OpeningDto(id: Int, val eco: String, val moves: String, val name: String, createdAt: Long) :
    BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): OpeningDto =
            OpeningDto(r[Openings.id], r[Openings.eco], r[Openings.moves], r[Openings.name], r[Openings.createdAt])
    }
}