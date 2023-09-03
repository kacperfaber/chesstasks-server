package com.chesstasks.cors

import com.chesstasks.Profiles
import com.chesstasks.Properties
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        if (Profiles.isProd()) setupProd() else setupDev()
    }
}

fun CORSConfig.setupDev() {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)

    // headers
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)

    // hosts
    anyHost()

    // origins
    allowOrigins { true }
}

private fun CORSConfig.setupProd() {
    val allowedOrigins by Properties.value<String>("$.security.cors.allowed-origins")
    val allowedHosts by Properties.value<String>("$.security.cors.allowed-hosts")

    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)

    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)

    allowedHosts
        .split(" ")
        .forEach { host -> allowHost(host, schemes = listOf("http", "https")) }

    val originArray = allowedOrigins.split(" ")

    allowOrigins { it in originArray }
}