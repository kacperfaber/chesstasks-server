package com.chesstasks.security

import com.chesstasks.Properties
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface PasswordHasher {
    suspend fun hash(rawPassword: String): String
    suspend fun comparePasswords(rawPassword: String, passwordHash: String): Boolean
}

class DevPasswordHasher : PasswordHasher{
    override suspend fun hash(rawPassword: String): String {
        return rawPassword
    }

    override suspend fun comparePasswords(rawPassword: String, passwordHash: String): Boolean {
        return rawPassword == passwordHash
    }
}

class ProdPasswordHasher : PasswordHasher {
    private val secretKey by Properties.value<String>("$.security.password-hasher.secret")
    private val algorithm = "PBKDF2WithHmacSHA512"
    private val iterations = 100_000
    private val keyLen = 256

    override suspend fun hash(rawPassword: String): String {
        val factory = SecretKeyFactory.getInstance(algorithm)
        val keySpec = PBEKeySpec(rawPassword.toCharArray(), secretKey.toByteArray(), iterations, keyLen)
        val secretKey = factory.generateSecret(keySpec)
        return String(secretKey.encoded)
    }

    override suspend fun comparePasswords(rawPassword: String, passwordHash: String): Boolean {
        return hash(rawPassword) == passwordHash
    }
}