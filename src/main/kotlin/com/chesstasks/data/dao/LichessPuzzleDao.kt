package com.chesstasks.data.dao

import com.chesstasks.data.dto.LichessPuzzleDto
import com.chesstasks.data.dto.LichessPuzzles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

interface LichessPuzzleDao {
    suspend fun getById(id: String): LichessPuzzleDto?
    suspend fun insert(id: String, fen: String, moves: String, ranking: Int): LichessPuzzleDto?
    suspend fun deleteById(id: String): Boolean
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

    override suspend fun insert(id: String, fen: String, moves: String, ranking: Int): LichessPuzzleDto? {
        return transaction {
            val insert = LichessPuzzles.insert {
                it[LichessPuzzles.id] = id
                it[LichessPuzzles.fen] = fen
                it[LichessPuzzles.moves] = moves
                it[LichessPuzzles.ranking] = ranking
            }

            resultRowToLichessPuzzleDto(insert.resultedValues?.singleOrNull() ?: return@transaction null)
        }
    }

    override suspend fun deleteById(id: String): Boolean {
        return transaction {
            LichessPuzzles.deleteWhere { LichessPuzzles.id eq id } > 0
        }
    }
}