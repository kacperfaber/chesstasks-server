package com.chesstasks.services.token

import com.google.gson.Gson

interface TokenWriter {
    fun writeToken(token: Token): String
}

class DevTokenWriter(private val gson: Gson) : TokenWriter {
    override fun writeToken(token: Token): String {
        return gson.toJson(token)
    }
}

class ProdTokenWriter : TokenWriter {
    override fun writeToken(token: Token): String {
        TODO("Not yet implemented")
    }
}