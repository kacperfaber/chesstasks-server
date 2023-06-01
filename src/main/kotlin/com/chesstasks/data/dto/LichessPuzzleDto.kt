package com.chesstasks.data.dto

import org.jetbrains.exposed.sql.Table

object LichessPuzzles : Table() {
    val id = varchar("id", 5)
    val fen = text("fen")
    val moves = varchar("moves", 255)
    val opening = varchar("opening", 64).nullable()
    // TODO: Use smaller data type to store 'opening'.
    val ranking = integer("ranking")
}

data class LichessPuzzleDto(val id: String, val fen: String, val moves: String, val ranking: Int, val opening: String?)