package com.chesstasks.security

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
    override suspend fun hash(rawPassword: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun comparePasswords(rawPassword: String, passwordHash: String): Boolean {
        TODO("Not yet implemented")
    }
}