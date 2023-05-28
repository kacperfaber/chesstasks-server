package com.chesstasks.services.token

import com.chesstasks.data.dao.TokenDao
import com.chesstasks.data.dto.TokenDto
import org.koin.core.annotation.Single

@Single
class TokenService(private val tokenReader: TokenReader, private val tokenWriter: TokenWriter, private val tokenDao: TokenDao) {
    fun readToken(tokenString: String): Token? {
        return tokenReader.readToken(tokenString)
    }

    fun writeToken(token: Token): String {
        return tokenWriter.writeToken(token)
    }

    suspend fun validateToken(token: Token): Int? = tokenDao.getTokenByUserIdAndSecret(token.userId, token.secret)?.id

    private suspend fun insertToken(userId: Int): TokenDto? {
        return tokenDao.tryInsertToken(userId)
    }

    suspend fun createToken(userId: Int): Token? {
        val tokenDto = insertToken(userId) ?: return null
        return Token(tokenDto.userId, tokenDto.secret)
    }

    suspend fun revokeToken(token: Token): Boolean {
        return tokenDao.deleteTokenByUserIdAndSecret(token.userId, token.secret)
    }
}