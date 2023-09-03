package com.chesstasks.services.token

import com.chesstasks.Properties
import com.google.gson.Gson
import org.apache.commons.codec.binary.Hex
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

interface TokenWriter {
    fun writeToken(token: Token): String
}

class DevTokenWriter(private val gson: Gson) : TokenWriter {
    override fun writeToken(token: Token): String {
        return gson.toJson(token)
    }
}

class ProdTokenWriter(private val gson: Gson) : TokenWriter {
    val secret by Properties.value<String>("$.security.tokens.secret")

    override fun writeToken(token: Token): String {
        val json = gson.toJson(token)
        val keyBytes = secret.getKeyBytes()
        val spec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, spec)

        val encryptedBytes = cipher.doFinal(json.toByteArray())
        return String(Hex.encodeHex(encryptedBytes))
    }
}