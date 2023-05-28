package com.chesstasks.services.token

import com.google.gson.Gson

interface TokenReader {
    fun readToken(tokenString: String): Token?
}

class DevTokenReader(private val gson: Gson) : TokenReader {
    override fun readToken(tokenString: String): Token? {
        return try {
            gson.fromJson(tokenString, Token::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

class ProdTokenReader : TokenReader {
    override fun readToken(tokenString: String): Token? {
        TODO("Not yet implemented")
    }
}