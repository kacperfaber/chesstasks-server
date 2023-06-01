package com.chesstasks.data.dao

import com.chesstasks.data.dto.LichessPuzzleDto
import com.chesstasks.data.dto.LichessPuzzles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

interface LichessPuzzleDao {
    suspend fun getById(id: String): LichessPuzzleDto?
}

@Single(binds = [LichessPuzzleDao::class])
class LichessPuzzleDaoImpl : LichessPuzzleDao {
    private fun resultRowToLichessPuzzleDto(resultRow: ResultRow): LichessPuzzleDto {
        return LichessPuzzleDto(
            resultRow[LichessPuzzles.id],
            resultRow[LichessPuzzles.fen],
            resultRow[LichessPuzzles.moves],
            resultRow[LichessPuzzles.ranking]
            // TODO: Add opening field later
        )
    }

    override suspend fun getById(id: String): LichessPuzzleDto? {
        return transaction {
            val resultRow = LichessPuzzles.select(LichessPuzzles.id eq id).singleOrNull() ?: return@transaction null
            return@transaction resultRowToLichessPuzzleDto(resultRow)
        }
    }
}