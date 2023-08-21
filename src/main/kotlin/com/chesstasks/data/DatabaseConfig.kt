package com.chesstasks.data

import io.ktor.server.application.*

fun Application.configureDb() {
    DatabaseFactory.init()

    trySetupTestDb()
}