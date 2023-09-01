package com.chesstasks.data.dto

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


object TrainingRankings : Table() {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val ranking = integer("ranking")

    override val primaryKey: PrimaryKey = PrimaryKey(userId)
}

class TrainingRankingDto(val userId: Int, val ranking: Int) {
    companion object {
        fun from(r: ResultRow): TrainingRankingDto = TrainingRankingDto(
            r[TrainingRankings.userId],
            r[TrainingRankings.ranking]
        )
    }
}