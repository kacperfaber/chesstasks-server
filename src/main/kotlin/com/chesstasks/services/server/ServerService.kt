package com.chesstasks.services.server

import org.koin.core.annotation.Single

@Single
class ServerService {
    fun getServerVersion(): String {
        return javaClass.classLoader.getResource("version")?.readText() ?: throw Exception("No version file!")
    }
}