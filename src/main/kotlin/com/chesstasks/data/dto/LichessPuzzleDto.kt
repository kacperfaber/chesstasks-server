package com.chesstasks.data.dto

import org.jetbrains.exposed.sql.Table

object LichessPuzzles : Table() {
    val id = varchar("id", 5)
    val fen = text("fen")
    val moves = varchar("moves", 255)
    val ranking = integer("ranking")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

data class LichessPuzzleDto(val id: String, val fen: String, val moves: String, val ranking: Int)