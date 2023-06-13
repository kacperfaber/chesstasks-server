package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.UserPuzzleDto
import com.chesstasks.data.dto.UserPuzzles
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

interface UserPuzzleDao {
    suspend fun getById(id: Int): UserPuzzleDto?
    suspend fun getByIdAndOwnerId(id: Int, ownerId: Int): UserPuzzleDto?
    suspend fun getByOwnerId(ownerId: Int): List<UserPuzzleDto>
    suspend fun createNew(ownerId: Int, fen: String, moves: String, ranking: Int): UserPuzzleDto?
    suspend fun deleteByIdAndOwnerId(id: Int, ownerId: Int): Boolean
    suspend fun deleteById(id: Int): Boolean
}

@Single
class UserPuzzleDaoImpl : UserPuzzleDao {
    override suspend fun getById(id: Int): UserPuzzleDto? {
        return dbQuery {
            UserPuzzles
                .select { UserPuzzles.id eq id }
                .map(UserPuzzleDto::from)
                .singleOrNull()
        }
    }

    override suspend fun getByIdAndOwnerId(id: Int, ownerId: Int): UserPuzzleDto? {
        return dbQuery {
            UserPuzzles.select { (UserPuzzles.id eq id) and (UserPuzzles.ownerId eq ownerId) }
                .map(UserPuzzleDto::from)
                .singleOrNull()
        }
    }

    override suspend fun getByOwnerId(ownerId: Int): List<UserPuzzleDto> {
        return dbQuery {
            UserPuzzles.select { (UserPuzzles.ownerId eq ownerId) }.map(UserPuzzleDto::from)
        }
    }

    override suspend fun createNew(ownerId: Int, fen: String, moves: String, ranking: Int): UserPuzzleDto? {
        return dbQuery {
            UserPuzzles.insert {
                it[UserPuzzles.fen] = fen
                it[UserPuzzles.moves] = moves
                it[UserPuzzles.ranking] = ranking
                it[UserPuzzles.ownerId] = ownerId
            }.resultedValues?.map(UserPuzzleDto::from)?.singleOrNull()
        }
    }

    override suspend fun deleteByIdAndOwnerId(id: Int, ownerId: Int): Boolean {
        return dbQuery {
            UserPuzzles.deleteWhere { (UserPuzzles.id eq id) and (UserPuzzles.ownerId eq ownerId) } > 0
        }
    }

    override suspend fun deleteById(id: Int): Boolean {
        return dbQuery {
            UserPuzzles.deleteWhere { UserPuzzles.id eq id } > 0
        }
    }
}