package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.data.dto.PuzzleDto
import com.chesstasks.data.dto.PuzzleDto.Companion.from
import com.chesstasks.data.dto.Puzzles
import com.chesstasks.data.dto.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single
class PuzzleDao {
    suspend fun getAll(limit: Int, skip: Long): List<PuzzleDto> {
        return dbQuery {
            (Puzzles innerJoin Users).selectAll().limit(limit, skip).map(::from)
        }
    }

    suspend fun getByOwnerId(ownerId: Int, limit: Int = 50, skip: Long = 50): List<PuzzleDto> {
        return dbQuery {
            (Puzzles innerJoin Users).select { Puzzles.ownerId eq ownerId }.limit(limit, skip).map(::from)
        }
    }

    suspend fun getById(id: Int): PuzzleDto? {
        return dbQuery {
            (Puzzles innerJoin Users).select { Puzzles.id eq id }.limit(1, 0L).map(::from).firstOrNull()
        }
    }

    suspend fun getAllByDatabase(database: PuzzleDatabase, limit: Int, skip: Long): List<PuzzleDto> {
        return dbQuery {
            (Puzzles innerJoin Users).select { Puzzles.database eq database }.limit(limit, skip).map(::from)
        }
    }
}