package com.chesstasks.security.auth

import com.chesstasks.data.dto.UserDto
import com.chesstasks.services.token.Token
import io.ktor.server.auth.*

data class TokenPrincipal(val token: Token, val user: UserDto) : Principal