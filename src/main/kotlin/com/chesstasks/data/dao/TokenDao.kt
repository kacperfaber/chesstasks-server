package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.TokenDto
import com.chesstasks.data.dto.Tokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

interface TokenDao {
    suspend fun getTokenByUserIdAndSecret(userId: Int, secret: String): TokenDto?
    suspend fun deleteTokenByUserIdAndSecret(userId: Int, secret: String): Boolean
}

@Single
class TokenDaoImpl : TokenDao {
    private fun resultRowToToken(resultRow: ResultRow): TokenDto {
        return TokenDto(
            id = resultRow[Tokens.id],
            createdAt = resultRow[Tokens.createdAt],
            secret = resultRow[Tokens.secret],
            userId = resultRow[Tokens.userId]
        )
    }

    override suspend fun getTokenByUserIdAndSecret(userId: Int, secret: String): TokenDto? = dbQuery {
        Tokens.select { (Tokens.secret eq secret) and (Tokens.userId eq userId) }.map(::resultRowToToken).singleOrNull()
    }

    override suspend fun deleteTokenByUserIdAndSecret(userId: Int, secret: String): Boolean {
        return dbQuery { Tokens.deleteWhere { (Tokens.userId eq userId) and (Tokens.secret eq secret) } } > 0
    }
}