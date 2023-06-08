package com.chesstasks.security.auth.session

import io.ktor.server.auth.*

class UserSession(val id: Int, val emailAddress: String, val username: String) : Principal