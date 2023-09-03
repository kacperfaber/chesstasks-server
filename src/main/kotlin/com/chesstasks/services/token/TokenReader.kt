package com.chesstasks.services.token

import com.chesstasks.Properties
import com.google.gson.Gson
import io.ktor.utils.io.core.*
import org.apache.commons.codec.binary.Hex
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.text.String
import kotlin.text.toByteArray

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

class ProdTokenReader(private val gson: Gson) : TokenReader {
    val secret by Properties.value<String>("$.security.tokens.secret")

    override fun readToken(tokenString: String): Token? {
        try {
            val secretKey = SecretKeySpec(secret.getKeyBytes(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            val byteArr = Hex.decodeHex(tokenString)

            val decryptedBytes = cipher.doFinal(byteArr)
            return gson.fromJson(String(decryptedBytes), Token::class.java)
        }

        catch (e: Exception) {
            return null
        }
    }
}