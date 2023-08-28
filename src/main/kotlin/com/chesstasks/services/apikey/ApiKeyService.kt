package com.chesstasks.services.apikey

import com.chesstasks.Properties
import org.koin.core.annotation.Single

@Single
class ApiKeyService {
    private val apiKey by Properties.value<String>("$.security.api-key")

    fun validateKey(apiKey: String): Boolean {
        return apiKey == this.apiKey
    }
}