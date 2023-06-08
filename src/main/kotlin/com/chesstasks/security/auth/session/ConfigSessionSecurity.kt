package com.chesstasks.security.auth.session

import io.ktor.server.auth.*

fun AuthenticationConfig.configSessionSecurity() {
    configAdminSession()
    configUserSession()
}